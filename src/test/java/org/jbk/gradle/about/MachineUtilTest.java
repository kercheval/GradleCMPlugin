package org.jbk.gradle.about;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

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
