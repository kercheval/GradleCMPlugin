package org.kercheval.gradle.util;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

public class GradleUtilTest {
    @Test
    public void test() {
        final Project project = ProjectBuilder.builder().build();
        final GradleUtil gradleUtil = new GradleUtil(project);
        final SortedProperties props = gradleUtil.getGradleInfo();

        try {
            props.store(System.out, "\nGradle Info\n");
        } catch (final IOException e) {
            fail();
        }

        assertNotNull(props);
        assertTrue(props.size() > 0);
    }
}
