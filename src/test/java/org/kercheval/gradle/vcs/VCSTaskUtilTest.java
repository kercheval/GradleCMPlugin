package org.kercheval.gradle.vcs;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.kercheval.gradle.buildversion.BuildVersionPlugin;
import org.kercheval.gradle.buildversion.BuildVersionTask;
import org.kercheval.gradle.gradlecm.GradleCMPlugin;
import org.kercheval.gradle.util.GradleUtil;

public class VCSTaskUtilTest
{
	@Test
	public void testValidateWorkspaceBranchName()
		throws IOException, InvalidRemoteException, TransportException, GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			Assert.assertEquals("master", repoUtil.getStandardRepo().getBranch());
			final VCSTaskUtil vcsUtil = new VCSTaskUtil("git", repoUtil.getStandardFile(), null);

			final Project project = ProjectBuilder.builder()
				.withProjectDir(repoUtil.getOriginFile()).build();
			project.apply(new LinkedHashMap<String, String>()
			{
				{
					put("plugin", GradleCMPlugin.BUILD_VERSION_PLUGIN);
				}
			});
			final GradleUtil gradleUtil = new GradleUtil(project);

			final BuildVersionTask versionTask = (BuildVersionTask) gradleUtil
				.getTask(BuildVersionPlugin.VERSION_TASK_NAME);

			try
			{
				vcsUtil.validateWorkspaceBranchName(versionTask, "master");
			}
			catch (final Exception e)
			{
				Assert.fail("Unexpected Exception");
			}

			try
			{
				vcsUtil.validateWorkspaceBranchName(versionTask, "failbranch");
				Assert.fail("Exception expected");
			}
			catch (final TaskExecutionException e)
			{}
		}
		finally
		{
			repoUtil.close();
		}
	}

	@Test
	public void testValidateWorkspaceIsClean()
		throws IOException, InvalidRemoteException, TransportException, GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			Assert.assertEquals("master", repoUtil.getStandardRepo().getBranch());
			final VCSTaskUtil vcsUtil = new VCSTaskUtil("git", repoUtil.getStandardFile(), null);

			final Project project = ProjectBuilder.builder()
				.withProjectDir(repoUtil.getOriginFile()).build();
			project.apply(new LinkedHashMap<String, String>()
			{
				{
					put("plugin", GradleCMPlugin.BUILD_VERSION_PLUGIN);
				}
			});
			final GradleUtil gradleUtil = new GradleUtil(project);

			final BuildVersionTask versionTask = (BuildVersionTask) gradleUtil
				.getTask(BuildVersionPlugin.VERSION_TASK_NAME);

			try
			{
				vcsUtil.validateWorkspaceIsClean(versionTask);
			}
			catch (final Exception e)
			{
				Assert.fail("Unexpected Exception");
			}

			final File newFile = new File(repoUtil.getStandardFile().getAbsolutePath()
				+ "/EmptySecondFile.txt");
			repoUtil.writeRandomContentFile(newFile);

			try
			{
				vcsUtil.validateWorkspaceIsClean(versionTask);
				Assert.fail("Exception expected");
			}
			catch (final TaskExecutionException e)
			{}
		}
		finally
		{
			repoUtil.close();
		}
	}
}
