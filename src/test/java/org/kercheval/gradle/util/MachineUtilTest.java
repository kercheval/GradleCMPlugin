package org.kercheval.gradle.util;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

public class MachineUtilTest {
    @Test
    public void test() {
        Project project = ProjectBuilder.builder().build();
        MachineUtil machineUtil = new MachineUtil(project);
        SortedProperties props = machineUtil.getMachineInfo();

        try {
            props.store(System.out, "\nMachine Info\n");
        } catch (IOException e) {
            fail();
        }

        assertNotNull(props);
        assertTrue(props.size() > 0);
    }
}
