package org.kercheval.gradle.buildrelease;

import java.io.*;
import java.util.LinkedHashMap;

import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.*;
import org.kercheval.gradle.gradlecm.GradleCMPlugin;
import org.kercheval.gradle.info.GradleInfoSource;
import org.kercheval.gradle.vcs.git.JGitTestRepository;

public class BuildReleaseMergeTaskTest {
    @Test
    public void testMerge()
            throws InvalidRemoteException, TransportException, IOException, GitAPIException {
        final JGitTestRepository repoUtil = new JGitTestRepository();
        try {
            final Project project =
                    ProjectBuilder.builder().withProjectDir(repoUtil.getStandardFile()).build();
            final GradleInfoSource gradleUtil = new GradleInfoSource(project);

            project.apply(new LinkedHashMap<String, Class<BuildReleasePlugin>>() {
                {
                    put("plugin", GradleCMPlugin.BUILD_RELEASE_PLUGIN);
                }
            });
            final BuildReleaseInitTask initTask =
                    (BuildReleaseInitTask) gradleUtil.getTask(BuildReleasePlugin.INIT_TASK_NAME);
            final BuildReleaseMergeTask mergeTask =
                    (BuildReleaseMergeTask) gradleUtil.getTask(BuildReleasePlugin.MERGE_TASK_NAME);

            initTask.setIgnoreorigin(true);

            try {
                mergeTask.doTask();
                Assert.fail("Exception expected");
            } catch (final TaskExecutionException e) {
                // Expected
            }

            initTask.setReleasebranch("master");
            initTask.setMainlinebranch("OriginBranch1");
            initTask.setRemoteorigin("myOrigin");

            mergeTask.doTask();

            initTask.setIgnoreorigin(false);

            final Ref originHead = repoUtil.getOriginRepo().getRef("refs/heads/master");
            Ref localHead = repoUtil.getStandardRepo().getRef("refs/heads/master");
            Assert.assertFalse(
                localHead.getObjectId().getName().equals(originHead.getObjectId().getName()));

            mergeTask.doTask();

            localHead = repoUtil.getStandardRepo().getRef("refs/heads/master");
            Assert.assertEquals(localHead.getObjectId().getName(),
                originHead.getObjectId().getName());

            final File newFile =
                    new File(repoUtil.getStandardFile().getAbsolutePath() + "/NotCleanFile.txt");
            repoUtil.writeRandomContentFile(newFile);

            try {
                mergeTask.doTask();
                Assert.fail("Expected Exception");
            } catch (final TaskExecutionException e) {
                // Expected
            }

        } finally {
            repoUtil.close();
        }
    }
}
