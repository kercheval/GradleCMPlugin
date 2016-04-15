package org.kercheval.gradle.gradlecm;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.kercheval.gradle.buildinfo.BuildInfoPlugin;
import org.kercheval.gradle.buildrelease.BuildReleasePlugin;
import org.kercheval.gradle.buildvcs.BuildVCSPlugin;
import org.kercheval.gradle.buildversion.BuildVersionPlugin;
import org.kercheval.gradle.info.GradleInfoSource;
import org.kercheval.gradle.vcs.git.JGitTestRepository;

public class GradleCMPluginTest
{

	@Test
	public void testGradleCMPlugin()
		throws InvalidRemoteException, TransportException, IOException, GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			final Project project = ProjectBuilder.builder()
				.withProjectDir(repoUtil.getOriginFile()).build();
			final GradleInfoSource gradleUtil = new GradleInfoSource(project);

			Assert.assertNull(gradleUtil.getTask(BuildInfoPlugin.INFO_TASK_NAME));

			project.apply(new LinkedHashMap<String, Class>()
			{
				{
					put("plugin", GradleCMPlugin.GRADLE_CM_PLUGIN);
				}
			});

			Assert.assertNotNull(gradleUtil.getTask(BuildInfoPlugin.INFO_TASK_NAME));
			Assert.assertNotNull(gradleUtil.getTask(BuildVersionPlugin.VERSION_TASK_NAME));
			Assert.assertNotNull(gradleUtil.getTask(BuildVersionPlugin.TAG_TASK_NAME));
			Assert.assertNotNull(gradleUtil.getTask(BuildReleasePlugin.INIT_TASK_NAME));
			Assert.assertNotNull(gradleUtil.getTask(BuildReleasePlugin.MERGE_TASK_NAME));
			Assert.assertNotNull(gradleUtil.getTask(BuildReleasePlugin.RELEASE_TASK_NAME));
			Assert.assertNotNull(gradleUtil.getTask(BuildVCSPlugin.VCS_TASK_NAME));
		}
		finally
		{
			repoUtil.close();
		}
	}
}
