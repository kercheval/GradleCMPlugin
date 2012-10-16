package org.kercheval.gradle.about;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

public class MachineUtilTest {
    @Test
    public void test() {
        MachineUtil machineUtil = new MachineUtil();
        Properties props = machineUtil.getMachineInfo();

        PropertyUtil.storeSorted(props, System.out, "\nMachine Info\n");
        assertNotNull(props);
        assertTrue(props.size() > 0);
    }
}
