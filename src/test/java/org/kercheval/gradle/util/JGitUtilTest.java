package org.kercheval.gradle.util;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

public class JGitUtilTest {
    @Test
    public void test() {
        final Project project = ProjectBuilder.builder().build();
        final JGitUtil jgu = new JGitUtil(project, new File("."));
        final SortedProperties props = jgu.getGitInfo();

        try {
            props.store(System.out, "\nGit Info\n");
        } catch (final IOException e) {
            fail();
        }

        assertNotNull(props);
        assertTrue(props.size() > 0);
    }
}
