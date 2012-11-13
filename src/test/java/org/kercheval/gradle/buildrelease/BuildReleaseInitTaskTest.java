package org.kercheval.gradle.buildrelease;

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
import org.kercheval.gradle.util.GradleUtil;
import org.kercheval.gradle.vcs.JGitTestRepository;

public class BuildReleaseInitTaskTest
{
	@Test
	public void testBranchCreation()
		throws InvalidRemoteException, TransportException, IOException, GitAPIException
	{
		final JGitTestRepository repoUtil = new JGitTestRepository();
		try
		{
			final Project project = ProjectBuilder.builder()
				.withProjectDir(repoUtil.getStandardFile()).build();
			final GradleUtil gradleUtil = new GradleUtil(project);

			project.apply(new LinkedHashMap<String, String>()
			{
				{
					put("plugin", GradleCMPlugin.BUILD_RELEASE_PLUGIN);
				}
			});
			final BuildReleaseInitTask task = (BuildReleaseInitTask) gradleUtil
				.getTask(BuildReleasePlugin.INIT_TASK_NAME);

			String mainline = "testmainlinebranch";
			String release = "testreleasebranch";

			task.setReleasebranch(release);
			task.setMainlinebranch(mainline);
			task.setRemoteorigin("myOrigin");
			task.setIgnoreorigin(false);

			Assert.assertNull(repoUtil.getStandardRepo().getRef("refs/heads/" + release));
			Assert.assertNull(repoUtil.getStandardRepo().getRef("refs/heads/" + mainline));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/heads/" + release));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/heads/" + mainline));

			task.doTask();

			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/heads/" + release));
			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/heads/" + mainline));
			Assert.assertNotNull(repoUtil.getOriginRepo().getRef("refs/heads/" + release));
			Assert.assertNotNull(repoUtil.getOriginRepo().getRef("refs/heads/" + mainline));

			mainline = "testmainlinebranch2";
			release = "testreleasebranch2";

			task.setReleasebranch(release);
			task.setMainlinebranch(mainline);
			task.setRemoteorigin("myOrigin");
			task.setIgnoreorigin(true);

			Assert.assertNull(repoUtil.getStandardRepo().getRef("refs/heads/" + release));
			Assert.assertNull(repoUtil.getStandardRepo().getRef("refs/heads/" + mainline));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/heads/" + release));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/heads/" + mainline));

			task.doTask();

			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/heads/" + release));
			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/heads/" + mainline));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/heads/" + release));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/heads/" + mainline));

			mainline = "OriginBranch4";
			release = "OriginBranch3";

			task.setReleasebranch(release);
			task.setMainlinebranch(mainline);
			task.setRemoteorigin("myOrigin");
			task.setIgnoreorigin(false);

			Assert.assertNull(repoUtil.getStandardRepo().getRef("refs/heads/" + release));
			Assert.assertNull(repoUtil.getStandardRepo().getRef("refs/heads/" + mainline));
			Assert.assertNotNull(repoUtil.getOriginRepo().getRef("refs/heads/" + release));
			Assert.assertNotNull(repoUtil.getOriginRepo().getRef("refs/heads/" + mainline));

			task.doTask();

			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/heads/" + release));
			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/heads/" + mainline));
			Assert.assertNotNull(repoUtil.getOriginRepo().getRef("refs/heads/" + release));
			Assert.assertNotNull(repoUtil.getOriginRepo().getRef("refs/heads/" + mainline));

			mainline = "StandardBranch1";
			release = "StandardBranch2";

			task.setReleasebranch(release);
			task.setMainlinebranch(mainline);
			task.setRemoteorigin("myOrigin");
			task.setIgnoreorigin(false);

			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/heads/" + release));
			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/heads/" + mainline));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/heads/" + release));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/heads/" + mainline));

			task.doTask();

			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/heads/" + release));
			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/heads/" + mainline));
			Assert.assertNotNull(repoUtil.getOriginRepo().getRef("refs/heads/" + release));
			Assert.assertNotNull(repoUtil.getOriginRepo().getRef("refs/heads/" + mainline));

			mainline = "CleanBranch1";
			release = "CleanBranch2";

			task.setReleasebranch(release);
			task.setMainlinebranch(mainline);
			task.setRemoteorigin("myOrigin");
			task.setIgnoreorigin(false);
			task.setOnlyifclean(true);

			final File newFile = new File(repoUtil.getStandardFile().getAbsolutePath()
				+ "/NotCleanFile.txt");
			repoUtil.writeRandomContentFile(newFile);

			Assert.assertNull(repoUtil.getStandardRepo().getRef("refs/heads/" + release));
			Assert.assertNull(repoUtil.getStandardRepo().getRef("refs/heads/" + mainline));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/heads/" + release));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/heads/" + mainline));

			try
			{
				task.doTask();
				Assert.fail("Exception Expected");
			}
			catch (final TaskExecutionException e)
			{
				// Expected
			}

			task.setOnlyifclean(false);
			Assert.assertNull(repoUtil.getStandardRepo().getRef("refs/heads/" + release));
			Assert.assertNull(repoUtil.getStandardRepo().getRef("refs/heads/" + mainline));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/heads/" + release));
			Assert.assertNull(repoUtil.getOriginRepo().getRef("refs/heads/" + mainline));

			task.doTask();

			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/heads/" + release));
			Assert.assertNotNull(repoUtil.getStandardRepo().getRef("refs/heads/" + mainline));
			Assert.assertNotNull(repoUtil.getOriginRepo().getRef("refs/heads/" + release));
			Assert.assertNotNull(repoUtil.getOriginRepo().getRef("refs/heads/" + mainline));
		}
		finally
		{
			repoUtil.close();
		}
	}
}
