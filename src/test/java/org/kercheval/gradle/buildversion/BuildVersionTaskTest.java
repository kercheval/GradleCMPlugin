package org.kercheval.gradle.buildversion;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.kercheval.gradle.gradlecm.GradleCMPlugin;
import org.kercheval.gradle.util.GradleUtil;
import org.kercheval.gradle.vcs.JGitTestRepository;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSGitImpl;
import org.kercheval.gradle.vcs.VCSTag;

public class BuildVersionTaskTest
{
	private void applyBuildVersionPlugin(final Project project)
	{
		project.apply(new LinkedHashMap<String, String>()
		{
			{
				put("plugin", GradleCMPlugin.BUILD_VERSION_PLUGIN);
			}
		});
	}

	@Test
	public void testBuildVersionTagTask()
		throws InvalidRemoteException, TransportException, IOException, GitAPIException,
		VCSException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			Project project = ProjectBuilder.builder().withProjectDir(repoUtil.getOriginFile())
				.build();
			applyBuildVersionPlugin(project);
			GradleUtil gradleUtil = new GradleUtil(project);
			BuildVersionTask versionTask = (BuildVersionTask) gradleUtil
				.getTask(BuildVersionPlugin.VERSION_TASK_NAME);
			Assert.assertNotNull(versionTask);
			versionTask.doTask();

			BuildVersionTagTask task = (BuildVersionTagTask) gradleUtil
				.getTask(BuildVersionPlugin.TAG_TASK_NAME);

			Assert.assertNotNull(task);
			task.setComment("We now have a comment");
			task.doTask();
			validateVersionTag(repoUtil, project);

			//
			// Reset project to try with changes resident and onlyifclean true.
			//
			project = ProjectBuilder.builder().withProjectDir(repoUtil.getOriginFile()).build();
			applyBuildVersionPlugin(project);
			gradleUtil = new GradleUtil(project);

			versionTask = (BuildVersionTask) gradleUtil
				.getTask(BuildVersionPlugin.VERSION_TASK_NAME);
			versionTask.doTask();

			new File(repoUtil.getOriginFile().getAbsolutePath() + "/foo.txt").createNewFile();
			task = (BuildVersionTagTask) gradleUtil.getTask(BuildVersionPlugin.TAG_TASK_NAME);
			task.setOnlyifclean(true);
			task.setComment("Testing only if clean");
			try
			{
				task.doTask();
				Assert.fail();
			}
			catch (final TaskExecutionException e)
			{
				// Expected
			}

			task.setOnlyifclean(false);
			task.doTask();
			validateVersionTag(repoUtil, project);
		}
		finally
		{
			repoUtil.close();
		}
	}

	@Test
	public void testBuildVersionTask()
		throws ParseException, InvalidRemoteException, TransportException, IOException,
		GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{

			final Project project = ProjectBuilder.builder()
				.withProjectDir(repoUtil.getOriginFile()).build();
			applyBuildVersionPlugin(project);
			final GradleUtil gradleUtil = new GradleUtil(project);
			final BuildVersionTask task = (BuildVersionTask) gradleUtil
				.getTask(BuildVersionPlugin.VERSION_TASK_NAME);

			Assert.assertNotNull(task);
			task.doTask();
			Assert.assertTrue(project.getVersion() instanceof BuildVersion);
			System.out.println(project.getVersion());
			task.setVersion(new BuildVersion("%M%.%m%-%d%-%t%", null));
			task.doTask();
			Assert.assertTrue(project.getVersion() instanceof BuildVersion);
			System.out.println(project.getVersion());
			Assert.assertEquals(3, ((BuildVersion) project.getVersion()).getMajor());
			Assert.assertEquals(1, ((BuildVersion) project.getVersion()).getMinor());
		}
		finally
		{
			repoUtil.close();
		}
	}

	private void validateVersionTag(final JGitTestRepository repoUtil, final Project project)
		throws VCSException
	{
		final BuildVersion version = ((BuildVersion) project.getVersion());
		final VCSGitImpl git = new VCSGitImpl(repoUtil.getOriginFile(), project.getLogger());
		final List<VCSTag> tagList = git.getTags(version.getValidatePattern());
		boolean found = false;
		for (final VCSTag tag : tagList)
		{
			if (tag.getName().equals(version.toString()))
			{
				found = true;
			}
		}
		Assert.assertTrue(found);
	}
}
