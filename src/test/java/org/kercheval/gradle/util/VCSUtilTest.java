package org.kercheval.gradle.util;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

public class VCSUtilTest {
    @Test
    public void test() {
        final Project project = ProjectBuilder.builder().build();
        final VCSUtil vcsUtil = new VCSUtil(project, new File("."));
        final SortedProperties props = vcsUtil.getVCSInfo();

        try {
            props.store(System.out, "\nVCS Info\n");
        } catch (final IOException e) {
            fail();
        }

        assertNotNull(props);
        assertTrue(props.size() > 0);
    }
}
