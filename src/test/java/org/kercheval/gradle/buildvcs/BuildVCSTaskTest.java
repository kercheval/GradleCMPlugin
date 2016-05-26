package org.kercheval.gradle.buildvcs;

import java.io.IOException;
import java.util.*;

import org.eclipse.jgit.api.errors.*;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.*;
import org.kercheval.gradle.gradlecm.GradleCMPlugin;
import org.kercheval.gradle.info.GradleInfoSource;
import org.kercheval.gradle.vcs.*;
import org.kercheval.gradle.vcs.git.JGitTestRepository;

public class BuildVCSTaskTest {

    @Test
    public void testGetType() throws InvalidRemoteException, TransportException, IOException,
            GitAPIException, VCSException {
        final JGitTestRepository repoUtil = new JGitTestRepository();
        try {
            final Project project =
                    ProjectBuilder.builder().withProjectDir(repoUtil.getOriginFile()).build();
            final GradleInfoSource gradleUtil = new GradleInfoSource(project);

            project.apply(new LinkedHashMap<String, Class<BuildVCSPlugin>>() {
                {
                    put("plugin", GradleCMPlugin.BUILD_VCS_PLUGIN);
                }
            });
            final BuildVCSTask task =
                    (BuildVCSTask) gradleUtil.getTask(BuildVCSPlugin.VCS_TASK_NAME);

            try {
                task.setType("Blat");
                Assert.fail("Expected Exception");
            } catch (final IllegalArgumentException e) {
                // expected
            }

            task.setType("none");

            Assert.assertEquals(VCSAccess.Type.NONE.toString().toLowerCase(), task.getType());
            Assert.assertTrue(task.isClean());

            final VCSStatus status = task.getStatus();
            Assert.assertNotNull(status);
            Assert.assertTrue(status.isClean());

            List<VCSTag> tagList = task.getAllTags();
            Assert.assertNotNull(tagList);
            Assert.assertTrue(tagList.isEmpty());

            tagList = task.getTags(".*");
            Assert.assertNotNull(tagList);
            Assert.assertTrue(tagList.isEmpty());

            final Properties props = task.getInfo();
            Assert.assertNotNull(props);
            Assert.assertTrue(props.isEmpty());

            try {
                task.createTag("name", "comment");
                Assert.fail("Expected exception");
            } catch (final VCSException e) {
                // expected
            }
            try {
                task.getBranchName();
                Assert.fail("Expected exception");
            } catch (final VCSException e) {
                // expected
            }
        } finally {
            repoUtil.close();
        }
    }
}
