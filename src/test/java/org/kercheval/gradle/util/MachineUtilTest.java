package org.kercheval.gradle.util;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

public class MachineUtilTest {
    @Test
    public void test() {
        MachineUtil machineUtil = new MachineUtil();
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
