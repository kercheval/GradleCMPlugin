package org.kercheval.gradle.buildversion;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Set;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.kercheval.gradle.buildvcs.BuildVCSPlugin;
import org.kercheval.gradle.gradlecm.GradleCMPlugin;
import org.kercheval.gradle.info.GradleInfoSource;
import org.kercheval.gradle.vcs.JGitTestRepository;

public class BuildVersionPluginTest
{
	@Test
	public void testPlugin()
		throws InvalidRemoteException, TransportException, IOException, GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			final Project project = ProjectBuilder.builder()
				.withProjectDir(repoUtil.getOriginFile()).build();
			final GradleInfoSource gradleUtil = new GradleInfoSource(project);

			Assert.assertNull(gradleUtil.getTask(BuildVersionPlugin.VERSION_TASK_NAME));

			project.apply(new LinkedHashMap<String, String>()
			{
				{
					put("plugin", GradleCMPlugin.BUILD_VERSION_PLUGIN);
				}
			});

			Assert.assertNotNull(gradleUtil.getTask(BuildVCSPlugin.VCS_TASK_NAME));

			final BuildVersionTask versionTask = (BuildVersionTask) gradleUtil
				.getTask(BuildVersionPlugin.VERSION_TASK_NAME);
			Assert.assertNotNull(versionTask);

			final BuildVersionTagTask tagTask = (BuildVersionTagTask) gradleUtil
				.getTask(BuildVersionPlugin.TAG_TASK_NAME);
			Assert.assertNotNull(tagTask);

			final Set<Object> dependsSet = tagTask.getDependsOn();
			Assert.assertEquals(2, dependsSet.size());
			Assert.assertTrue(dependsSet.contains(":" + BuildVersionPlugin.VERSION_TASK_NAME));
		}
		finally
		{
			repoUtil.close();
		}
	}
}
