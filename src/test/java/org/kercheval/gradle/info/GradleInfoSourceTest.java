package org.kercheval.gradle.info;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Test;
import org.kercheval.gradle.info.GradleInfoSource;
import org.kercheval.gradle.info.SortedProperties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

public class GradleInfoSourceTest
{
	@Test
	public void test()
	{
		final Project project = ProjectBuilder.builder().build();
		final GradleInfoSource gradleUtil = new GradleInfoSource(project);
		final SortedProperties props = gradleUtil.getInfo();

		try
		{
			props.store(System.out, "\nGradle Info\n");
		}
		catch (final IOException e)
		{
			fail();
		}

		assertNotNull(props);
		assertTrue(props.size() > 0);
	}
}
