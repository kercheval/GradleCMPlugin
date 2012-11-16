package org.kercheval.gradle.info;

import org.junit.Test;
import org.kercheval.gradle.info.JenkinsInfoSource;
import org.kercheval.gradle.info.SortedProperties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

public class JenkinsInfoSourceTest
{
	@Test
	public void test()
	{
		final JenkinsInfoSource jenkinsUtil = new JenkinsInfoSource();
		final SortedProperties props = jenkinsUtil.getInfo();

		try
		{
			props.store(System.out, "\nJenkins Info\n");
		}
		catch (final IOException e)
		{
			fail();
		}

		assertNotNull(props);
		assertTrue(props.size() > 0);
	}
}
