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
        Project project = ProjectBuilder.builder().build();
        GradleUtil gradleUtil = new GradleUtil(project);
        SortedProperties props = gradleUtil.getGradleInfo();

        try {
            props.store(System.out, "\nGradle Info\n");
        } catch (IOException e) {
            fail();
        }

        assertNotNull(props);
        assertTrue(props.size() > 0);
    }
}
