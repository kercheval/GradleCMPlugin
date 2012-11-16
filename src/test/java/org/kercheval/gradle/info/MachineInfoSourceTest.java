package org.kercheval.gradle.info;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Test;
import org.kercheval.gradle.info.MachineInfoSource;
import org.kercheval.gradle.info.SortedProperties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

public class MachineInfoSourceTest
{
	@Test
	public void test()
	{
		final Project project = ProjectBuilder.builder().build();
		final MachineInfoSource machineUtil = new MachineInfoSource(project);
		final SortedProperties props = machineUtil.getInfo();

		try
		{
			props.store(System.out, "\nMachine Info\n");
		}
		catch (final IOException e)
		{
			fail();
		}

		assertNotNull(props);
		assertTrue(props.size() > 0);
	}
}
