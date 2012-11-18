package org.kercheval.gradle.buildrelease;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.PersonIdent;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.kercheval.gradle.buildvcs.BuildVCSPlugin;
import org.kercheval.gradle.buildvcs.BuildVCSTask;
import org.kercheval.gradle.buildversion.BuildVersion;
import org.kercheval.gradle.buildversion.BuildVersionPlugin;
import org.kercheval.gradle.buildversion.BuildVersionTask;
import org.kercheval.gradle.gradlecm.GradleCMPlugin;
import org.kercheval.gradle.info.GradleInfoSource;
import org.kercheval.gradle.vcs.git.JGitTestRepository;

public class BuildReleasePluginTest
{
	@Test
	public void testTagAndPush()
		throws InvalidRemoteException, TransportException, IOException, GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		new Git(repoUtil.getStandardRepo()).fetch().setRemote("myOrigin").call();
		final String refLocalBranch = "myOrigin/master";
		new Git(repoUtil.getStandardRepo()).merge()
			.include(repoUtil.getStandardRepo().getRef(refLocalBranch)).call();

		try
		{
			final Project project = ProjectBuilder.builder()
				.withProjectDir(repoUtil.getStandardFile()).build();
			project.apply(new LinkedHashMap<String, String>()
			{
				{
					put("plugin", GradleCMPlugin.BUILD_RELEASE_PLUGIN);
				}
			});

			final GradleInfoSource gradleUtil = new GradleInfoSource(project);

			final BuildReleaseInitTask initTask = (BuildReleaseInitTask) gradleUtil
				.getTask(BuildReleasePlugin.INIT_TASK_NAME);
			final BuildReleaseTask releaseTask = (BuildReleaseTask) gradleUtil
				.getTask(BuildReleasePlugin.RELEASE_TASK_NAME);
			Assert.assertNotNull(releaseTask);

			final Set<Object> dependsSet = releaseTask.getDependsOn();
			Assert.assertEquals(3, dependsSet.size());
			Assert.assertTrue(dependsSet.contains(":" + BuildReleasePlugin.MERGE_TASK_NAME));
			Assert.assertTrue(dependsSet.contains(":" + initTask.getUploadtask()));

			Assert.assertNotNull(gradleUtil.getTask(BuildVersionPlugin.VERSION_TASK_NAME));
			Assert.assertNotNull(gradleUtil.getTask(BuildVersionPlugin.TAG_TASK_NAME));
			Assert.assertNotNull(gradleUtil.getTask(BuildReleasePlugin.INIT_TASK_NAME));
			Assert.assertNotNull(gradleUtil.getTask(BuildReleasePlugin.MERGE_TASK_NAME));
			Assert.assertNotNull(gradleUtil.getTask(BuildVCSPlugin.VCS_TASK_NAME));

			final BuildReleasePlugin plugin = (BuildReleasePlugin) project.getPlugins().findPlugin(
				GradleCMPlugin.BUILD_RELEASE_PLUGIN);
			final BuildReleaseInitTask releaseInitTask = (BuildReleaseInitTask) gradleUtil
				.getTask(BuildReleasePlugin.INIT_TASK_NAME);
			final BuildVersionTask versionTask = (BuildVersionTask) gradleUtil
				.getTask(BuildVersionPlugin.VERSION_TASK_NAME);
			final BuildVCSTask vcsTask = (BuildVCSTask) gradleUtil
				.getTask(BuildVCSPlugin.VCS_TASK_NAME);

			//
			// Need the version to do tagging
			//
			versionTask.doTask();

			//
			// Validate branch validation logic
			//
			vcsTask.setType("none");
			plugin.tagAndPush(project, releaseInitTask, true);

			vcsTask.setType("git");
			try
			{
				plugin.tagAndPush(project, releaseInitTask, true);
				Assert.fail("Expected exception");
			}
			catch (final TaskExecutionException e)
			{
				// Expected
			}

			//
			// Done for side effect testing (should not throw exception)
			//
			plugin.tagAndPush(project, releaseInitTask, false);
			releaseInitTask.setReleasebranch("master");
			releaseInitTask.setMainlinebranch("OriginBranch1");
			releaseInitTask.setRemoteorigin("myOrigin");

			//
			// Set ignore origin to true, then test new tag is present
			//
			releaseInitTask.setIgnoreorigin(true);
			Assert.assertNull(repoUtil.getStandardRepo().getRef(
				"refs/tags/" + project.getVersion().toString()));
			plugin.tagAndPush(project, releaseInitTask, true);
			Assert.assertNotNull(repoUtil.getStandardRepo().getRef(
				"refs/tags/" + project.getVersion().toString()));

			//
			// Set ignore origin to false, then test new tag is present and
			// That change in release is now at origin
			//
			((BuildVersion) project.getVersion()).incrementMinor();
			releaseInitTask.setIgnoreorigin(false);
			plugin.tagAndPush(project, releaseInitTask, true);

			//
			// Create a file and check, then set the onlyifclean to
			// false and check again.
			//
			final File newFile = new File(repoUtil.getStandardFile().getAbsolutePath()
				+ "/TestFileForTagAndPush.txt");
			repoUtil.writeRandomContentFile(newFile);
			new Git(repoUtil.getStandardRepo()).add().addFilepattern(".").call();

			try
			{
				plugin.tagAndPush(project, releaseInitTask, true);
				Assert.fail("Exception expected");
			}
			catch (final TaskExecutionException e)
			{
				// expected
			}

			releaseInitTask.setOnlyifclean(false);
			((BuildVersion) project.getVersion()).incrementMinor();
			plugin.tagAndPush(project, releaseInitTask, true);
			releaseInitTask.setOnlyifclean(true);

			new Git(repoUtil.getStandardRepo()).commit()
				.setCommitter(new PersonIdent("JUNIT", "JUNIT@dev.build"))
				.setMessage("First commit into origin repository").call();

			((BuildVersion) project.getVersion()).incrementMinor();
			plugin.tagAndPush(project, releaseInitTask, true);
			Assert.assertNotNull(repoUtil.getStandardRepo().getRef(
				"refs/tags/" + project.getVersion().toString()));
			Assert.assertNotNull(repoUtil.getOriginRepo().getRef(
				"refs/tags/" + project.getVersion().toString()));
		}
		finally
		{
			repoUtil.close();
		}

	}
}
