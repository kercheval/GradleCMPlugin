package org.kercheval.gradle.buildvcs;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.kercheval.gradle.util.GradleUtil;
import org.kercheval.gradle.vcs.IVCSAccess;
import org.kercheval.gradle.vcs.JGitTestRepository;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSStatus;
import org.kercheval.gradle.vcs.VCSTag;

public class BuildVCSTaskTest
{

	@Test
	public void testGetType()
		throws InvalidRemoteException, TransportException, IOException, GitAPIException,
		VCSException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			final Project project = ProjectBuilder.builder()
				.withProjectDir(repoUtil.getOriginFile()).build();
			final GradleUtil gradleUtil = new GradleUtil(project);

			project.apply(new LinkedHashMap<String, String>()
			{
				{
					put("plugin", "buildvcs");
				}
			});
			final BuildVCSTask task = (BuildVCSTask) gradleUtil.getTask("buildvcs");

			try
			{
				task.setType("Blat");
				Assert.fail("Expected Exception");
			}
			catch (final IllegalArgumentException e)
			{
				// expected
			}

			task.setType("none");

			Assert.assertEquals(IVCSAccess.Type.NONE.toString().toLowerCase(), task.getType());
			Assert.assertTrue(task.isClean());

			final VCSStatus status = task.getStatus();
			Assert.assertNotNull(status);
			Assert.assertTrue(status.isClean());

			List<VCSTag> tagList = task.getAllTags();
			Assert.assertNotNull(tagList);
			Assert.assertTrue(tagList.isEmpty());

			tagList = task.getTags(".*");
			Assert.assertNotNull(tagList);
			Assert.assertTrue(tagList.isEmpty());

			final Properties props = task.getInfo();
			Assert.assertNotNull(props);
			Assert.assertTrue(props.isEmpty());

			try
			{
				task.createTag("name", "comment");
				Assert.fail("Expected exception");
			}
			catch (final VCSException e)
			{
				// expected
			}
			try
			{
				task.getBranchName();
				Assert.fail("Expected exception");
			}
			catch (final VCSException e)
			{
				// expected
			}
		}
		finally
		{
			repoUtil.close();
		}
	}
}
