package org.kercheval.gradle.vcs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.kercheval.gradle.util.SortedProperties;

public class VCSGitImplTest
{
	@Test
	public void testGetBranch()
		throws VCSException, InvalidRemoteException, TransportException, IOException,
		GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			final VCSGitImpl git = (VCSGitImpl) VCSGitImpl.getInstance(repoUtil.getOriginfile(),
				null);
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
	public void testGetInfo()
		throws VCSException, InvalidRemoteException, TransportException, IOException,
		GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			final Project project = ProjectBuilder.builder().build();
			final SortedProperties props = VCSAccessFactory.getCurrentVCS(repoUtil.getOriginfile(),
				project.getLogger()).getInfo();

			try
			{
				props.store(System.out, "\nVCS Info\n");
			}
			catch (final IOException e)
			{
				fail();
			}

			assertNotNull(props);
			assertTrue(props.size() > 0);
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
			final VCSGitImpl git = (VCSGitImpl) VCSGitImpl.getInstance(repoUtil.getOriginfile(),
				null);
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
}
