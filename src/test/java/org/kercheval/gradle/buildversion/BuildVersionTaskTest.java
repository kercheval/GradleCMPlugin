package org.kercheval.gradle.buildversion;

import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.plugins.DefaultObjectConfigurationAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.kercheval.gradle.vcs.JGitTestRepository;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSGitImpl;
import org.kercheval.gradle.vcs.VCSTag;

public class BuildVersionTaskTest
{
	private DefaultTask getTask(final Project project, final String taskname)
	{
		project.apply(new Closure<DefaultObjectConfigurationAction>(project, project)
		{
			@SuppressWarnings("unused")
			public Object doCall(final DefaultObjectConfigurationAction pluginAction)
			{
				pluginAction.plugin("buildversion");

				return pluginAction;
			}
		});

		final Map<String, Task> tasknameMap = new HashMap<String, Task>();

		for (final Task task : project.getAllTasks(false).get(project))
		{
			System.out.println(task.getName());
			tasknameMap.put(task.getName(), task);
		}

		return (DefaultTask) tasknameMap.get(taskname);
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

			BuildVersionTask versionTask = (BuildVersionTask) getTask(project, "buildversion");
			Assert.assertNotNull(versionTask);
			versionTask.doTask();

			BuildVersionTagTask task = (BuildVersionTagTask) getTask(project, "buildversiontag");

			Assert.assertNotNull(task);
			task.setComment("We now have a comment");
			task.doTask();
			validateVersionTag(repoUtil, project);

			//
			// Reset project to try with changes resident and onlyifclean true.
			//
			project = ProjectBuilder.builder().withProjectDir(repoUtil.getOriginFile()).build();

			versionTask = (BuildVersionTask) getTask(project, "buildversion");
			versionTask.doTask();

			new File(repoUtil.getOriginFile().getAbsolutePath() + "/foo.txt").createNewFile();
			task = (BuildVersionTagTask) getTask(project, "buildversiontag");
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
			final BuildVersionTask task = (BuildVersionTask) getTask(project, "buildversion");

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
		final VCSGitImpl git = (VCSGitImpl) VCSGitImpl.getInstance(repoUtil.getOriginFile(),
			project.getLogger());
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
