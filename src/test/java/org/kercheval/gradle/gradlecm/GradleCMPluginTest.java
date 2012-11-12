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
import org.kercheval.gradle.util.GradleUtil;
import org.kercheval.gradle.vcs.JGitTestRepository;

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
			final GradleUtil gradleUtil = new GradleUtil(project);

			Assert.assertNull(gradleUtil.getTask("buildinfo"));

			project.apply(new LinkedHashMap<String, String>()
			{
				{
					put("plugin", "gradlecm");
				}
			});

			Assert.assertNotNull(gradleUtil.getTask("buildinfo"));
			Assert.assertNotNull(gradleUtil.getTask("buildversion"));
			Assert.assertNotNull(gradleUtil.getTask("buildversiontag"));
			Assert.assertNotNull(gradleUtil.getTask("buildreleaseinit"));
			Assert.assertNotNull(gradleUtil.getTask("buildreleasemerge"));
			Assert.assertNotNull(gradleUtil.getTask("buildvcs"));
		}
		finally
		{
			repoUtil.close();
		}

	}

}
