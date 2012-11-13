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
import org.kercheval.gradle.util.GradleUtil;
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
			final GradleUtil gradleUtil = new GradleUtil(project);

			Assert.assertNull(gradleUtil.getTask("buildversion"));

			project.apply(new LinkedHashMap<String, String>()
			{
				{
					put("plugin", "buildversion");
				}
			});

			Assert.assertNotNull(gradleUtil.getTask("buildvcs"));

			final BuildVersionTask versionTask = (BuildVersionTask) gradleUtil
				.getTask("buildversion");
			Assert.assertNotNull(versionTask);

			final BuildVersionTagTask tagTask = (BuildVersionTagTask) gradleUtil
				.getTask("buildversiontag");
			Assert.assertNotNull(tagTask);

			final Set<Object> dependsSet = tagTask.getDependsOn();
			System.out.println(dependsSet);
			Assert.assertEquals(2, dependsSet.size());
			Assert.assertTrue(dependsSet.contains(":buildversion"));
		}
		finally
		{
			repoUtil.close();
		}
	}
}
