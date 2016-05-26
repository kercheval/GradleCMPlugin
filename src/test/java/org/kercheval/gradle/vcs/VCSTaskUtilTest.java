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
import org.kercheval.gradle.gradlecm.GradleCMPlugin;
import org.kercheval.gradle.vcs.git.JGitTestRepository;

public class VCSTaskUtilTest
{
	@SuppressWarnings("rawtypes")
	@Test
	public void testValidateWorkspaceBranchName()
		throws IOException, InvalidRemoteException, TransportException, GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			Assert.assertEquals("master", repoUtil.getStandardRepo().getBranch());

			final Project project = ProjectBuilder.builder()
				.withProjectDir(repoUtil.getOriginFile()).build();
			project.apply(new LinkedHashMap<String, Class>()
			{
				{
					put("plugin", GradleCMPlugin.BUILD_VERSION_PLUGIN);
				}
			});
			final VCSTaskUtil vcsUtil = new VCSTaskUtil(project);
			try
			{
				vcsUtil.validateWorkspaceBranchName("master");
			}
			catch (final Exception e)
			{
				Assert.fail("Unexpected Exception");
			}

			try
			{
				vcsUtil.validateWorkspaceBranchName("failbranch");
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

	@SuppressWarnings("rawtypes")
	@Test
	public void testValidateWorkspaceIsClean()
		throws IOException, InvalidRemoteException, TransportException, GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			Assert.assertEquals("master", repoUtil.getStandardRepo().getBranch());
			final Project project = ProjectBuilder.builder()
				.withProjectDir(repoUtil.getStandardFile()).build();
			project.apply(new LinkedHashMap<String, Class>()
			{
				{
					put("plugin", GradleCMPlugin.BUILD_VERSION_PLUGIN);
				}
			});
			final VCSTaskUtil vcsUtil = new VCSTaskUtil(project);
			try
			{
				vcsUtil.validateWorkspaceIsClean();
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
				vcsUtil.validateWorkspaceIsClean();
				Assert.fail("Exception expected");
			}
			catch (final TaskExecutionException e)
			{
				Assert.assertTrue(e.getCause().getMessage()
					.contains("Untracked: [EmptySecondFile.txt]"));
			}
		}
		finally
		{
			repoUtil.close();
		}
	}
}
