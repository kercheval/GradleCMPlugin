package org.kercheval.gradle.vcs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.kercheval.gradle.util.SortedProperties;

public class VCSGitImplTest
{
	private void createAndValidateBranchOnMainline(final JGitTestRepository repoUtil,
		final String branchName, final String originName, final boolean ignoreOrigin,
		final boolean verifyOrigin)
		throws VCSException
	{
		final VCSGitImpl git = new VCSGitImpl(repoUtil.getStandardFile(), null);
		git.createBranch(branchName, originName, ignoreOrigin);
		Map<String, Ref> refMap = repoUtil.getStandardRepo().getAllRefs();
		Assert.assertTrue(refMap.containsKey("refs/heads/" + branchName));
		if (!ignoreOrigin)
		{
			Assert.assertTrue(refMap.containsKey("refs/remotes/" + originName + "/" + branchName));
			refMap = repoUtil.getOriginRepo().getAllRefs();
			Assert.assertTrue(refMap.containsKey("refs/heads/" + branchName));
		}
		else if (verifyOrigin)
		{
			Assert.assertFalse(refMap.containsKey("refs/remotes/" + originName + "/" + branchName));
			refMap = repoUtil.getOriginRepo().getAllRefs();
			Assert.assertFalse(refMap.containsKey("refs/heads/" + branchName));
		}
	}

	private void createAndValidateBranchOnOrigin(final JGitTestRepository repoUtil,
		final String branchName, final String originName, final boolean ignoreOrigin)
		throws VCSException
	{
		final VCSGitImpl git = new VCSGitImpl(repoUtil.getOriginFile(), null);
		git.createBranch(branchName, originName, ignoreOrigin);
		final Map<String, Ref> refMap = repoUtil.getOriginRepo().getAllRefs();
		Assert.assertTrue(refMap.containsKey("refs/heads/" + branchName));
	}

	@Test
	public void testCreateBranch()
		throws InvalidRemoteException, TransportException, IOException, GitAPIException,
		VCSException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			final String originName = "myOrigin";
			createAndValidateBranchOnOrigin(repoUtil, "OriginIgnoreOrigin_New", "myOrigin", true);
			createAndValidateBranchOnOrigin(repoUtil, "OriginNotIgnoreOrigin_New", "myOrigin",
				false);
			createAndValidateBranchOnMainline(repoUtil, "BranchIgnoreOrigin_New", originName, true,
				true);
			createAndValidateBranchOnMainline(repoUtil, "BranchNotIgnoreOrigin_New", originName,
				false, true);
			createAndValidateBranchOnMainline(repoUtil, "StandardBranch1", originName, true, true);
			createAndValidateBranchOnMainline(repoUtil, "StandardBranch2", originName, false, true);
			createAndValidateBranchOnMainline(repoUtil, "OriginBranch1", originName, true, false);
			createAndValidateBranchOnMainline(repoUtil, "OriginBranch2", originName, false, false);
			createAndValidateBranchOnMainline(repoUtil, "OriginBranch3", originName, true, false);
			createAndValidateBranchOnMainline(repoUtil, "OriginBranch4", originName, false, false);
		}
		finally
		{
			repoUtil.close();
		}
	}

	@Test
	public void testGetBranch()
		throws VCSException, InvalidRemoteException, TransportException, IOException,
		GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			final VCSGitImpl git = new VCSGitImpl(repoUtil.getOriginFile(), null);
			final String branchName = git.getBranchName();
			System.out.println("Found branch - " + branchName);
			Assert.assertNotNull(branchName);
			Assert.assertEquals("master", branchName);
		}
		finally
		{
			repoUtil.close();
		}
	}

	@Test
	public void testGetFetchAndMergeAndPush()
		throws VCSException, InvalidRemoteException, TransportException, IOException,
		GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			Ref originHead = repoUtil.getStandardRepo().getRef("refs/remotes/myOrigin/master");
			Ref localHead = repoUtil.getStandardRepo().getRef("refs/heads/master");
			Assert.assertEquals(localHead.getObjectId().getName(), originHead.getObjectId()
				.getName());
			final VCSGitImpl git = new VCSGitImpl(repoUtil.getStandardFile(), null);

			git.fetch("myOrigin");
			originHead = repoUtil.getStandardRepo().getRef("refs/remotes/myOrigin/master");
			localHead = repoUtil.getStandardRepo().getRef("refs/heads/master");
			Assert.assertFalse(localHead.getObjectId().getName()
				.equals(originHead.getObjectId().getName()));

			git.mergeBranch("myOrigin");
			originHead = repoUtil.getStandardRepo().getRef("refs/remotes/myOrigin/master");
			localHead = repoUtil.getStandardRepo().getRef("refs/heads/master");
			Assert.assertEquals(localHead.getObjectId().getName(), originHead.getObjectId()
				.getName());

			final File newFile = new File(repoUtil.getStandardFile().getAbsolutePath()
				+ "/EmptyThirdFile.txt");
			repoUtil.writeRandomContentFile(newFile);
			new Git(repoUtil.getStandardRepo()).add().addFilepattern(".").call();
			new Git(repoUtil.getStandardRepo()).commit()
				.setCommitter(new PersonIdent("JUNIT", "JUNIT@dev.build"))
				.setMessage("First commit into origin repository").call();
			repoUtil.getStandardRepo().getRef("refs/heads/master");
			final Ref newLocalHead = repoUtil.getStandardRepo().getRef("refs/heads/master");
			Assert.assertFalse(localHead.getObjectId().getName()
				.equals(newLocalHead.getObjectId().getName()));
			new Git(repoUtil.getStandardRepo()).tag().setName("NEW_TAG").setMessage("Test of Push")
				.call();
			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/tags/NEW_TAG"));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/tags/NEW_TAG"));

			git.pushBranch("master", "myOrigin", true);
			originHead = repoUtil.getStandardRepo().getRef("refs/remotes/myOrigin/master");
			localHead = repoUtil.getStandardRepo().getRef("refs/heads/master");
			Assert.assertEquals(localHead.getObjectId().getName(), originHead.getObjectId()
				.getName());
			Assert.assertNotNull(repoUtil.getOriginRepo().getRef("refs/tags/NEW_TAG"));
			new Git(repoUtil.getStandardRepo()).tag().setName("ANOTHER_NEW_TAG")
				.setMessage("Test of Push").call();
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/tags/ANOTHER_NEW_TAG"));
			git.pushBranch("master", "myOrigin", false);
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/tags/ANOTHER_NEW_TAG"));
		}
		finally
		{
			repoUtil.close();
		}
	}

	@Test
	public void testGetInfo()
		throws VCSException, InvalidRemoteException, TransportException, IOException,
		GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			final Project project = ProjectBuilder.builder().build();
			final SortedProperties props = VCSAccessFactory.getCurrentVCS("Git",
				repoUtil.getOriginFile(), project.getLogger()).getInfo();

			try
			{
				props.store(System.out, "\nVCS Info\n");
			}
			catch (final IOException e)
			{
				Assert.fail();
			}

			Assert.assertNotNull(props);
			Assert.assertTrue(props.size() > 0);
		}
		finally
		{
			repoUtil.close();
		}

	}

	@Test
	public void testGetStatus()
		throws VCSException, InvalidRemoteException, TransportException, IOException,
		GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			final VCSGitImpl git = new VCSGitImpl(repoUtil.getOriginFile(), null);
			VCSStatus status = git.getStatus();
			Assert.assertNotNull(status);
			Assert.assertTrue(status.isClean());

			final File newFile = new File(repoUtil.getOriginFile().getAbsolutePath()
				+ "/StatusChange.txt");
			newFile.createNewFile();

			status = git.getStatus();
			Assert.assertNotNull(status);
			Assert.assertFalse(status.isClean());
			Assert.assertEquals("[StatusChange.txt]", status.getUntracked().toString());
		}
		finally
		{
			repoUtil.close();
		}
	}

	@Test
	public void testGetTags()
		throws VCSException, InvalidRemoteException, TransportException, IOException,
		GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			final VCSGitImpl git = new VCSGitImpl(repoUtil.getOriginFile(), null);
			List<VCSTag> tagList = git.getAllTags();
			Assert.assertFalse(tagList.isEmpty());

			tagList = git.getTags("^JUNIT_Tag_Filter$");
			Assert.assertTrue(tagList.isEmpty());
			git.createTag(new VCSTag("JUNIT_Tag_Filter", "Test tag add"));
			tagList = git.getTags("^JUNIT_Tag_Filter$");
			Assert.assertTrue(tagList.size() == 1);
		}
		finally
		{
			repoUtil.close();
		}
	}

	@Test
	public void testMergeFail()
		throws VCSException, InvalidRemoteException, TransportException, IOException,
		GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{

			Ref originHead = repoUtil.getStandardRepo().getRef("refs/remotes/myOrigin/master");
			Ref localHead = repoUtil.getStandardRepo().getRef("refs/heads/master");
			Assert.assertEquals(localHead.getObjectId().getName(), originHead.getObjectId()
				.getName());
			final VCSGitImpl git = new VCSGitImpl(repoUtil.getStandardFile(), null);
			git.fetch("myOrigin");
			originHead = repoUtil.getStandardRepo().getRef("refs/remotes/myOrigin/master");
			localHead = repoUtil.getStandardRepo().getRef("refs/heads/master");
			Assert.assertFalse(localHead.getObjectId().getName()
				.equals(originHead.getObjectId().getName()));

			final File newFile = new File(repoUtil.getStandardFile().getAbsolutePath()
				+ "/EmptySecondFile.txt");
			repoUtil.writeRandomContentFile(newFile);
			new Git(repoUtil.getStandardRepo()).add().addFilepattern(".").call();
			new Git(repoUtil.getStandardRepo()).commit()
				.setCommitter(new PersonIdent("JUNIT", "JUNIT@dev.build"))
				.setMessage("First commit into origin repository").call();
			final Ref oldLocalHead = repoUtil.getStandardRepo().getRef("refs/heads/master");

			try
			{
				git.mergeBranch("myOrigin");
				Assert.fail("Merge conflict expected");
			}
			catch (final VCSException e)
			{
				localHead = repoUtil.getStandardRepo().getRef("refs/heads/master");
				Assert.assertEquals(localHead.getObjectId().getName(), oldLocalHead.getObjectId()
					.getName());
			}
		}
		finally
		{
			repoUtil.close();
		}
	}

	@Test
	public void testPushFail()
		throws InvalidRemoteException, TransportException, IOException, GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{

			final File newFile = new File(repoUtil.getStandardFile().getAbsolutePath()
				+ "/EmptySecondFile.txt");
			repoUtil.writeRandomContentFile(newFile);
			new Git(repoUtil.getStandardRepo()).add().addFilepattern(".").call();
			new Git(repoUtil.getStandardRepo()).commit()
				.setCommitter(new PersonIdent("JUNIT", "JUNIT@dev.build"))
				.setMessage("First commit into origin repository").call();
			final VCSGitImpl git = new VCSGitImpl(repoUtil.getStandardFile(), null);
			try
			{
				git.pushBranch("master", "myOrigin", true);
				Assert.fail("Expected exception");
			}
			catch (final VCSException e)
			{
				// Expected
			}
		}
		finally
		{
			repoUtil.close();
		}
	}

}
