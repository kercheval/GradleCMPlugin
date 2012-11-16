package org.kercheval.gradle.info;

import org.junit.Assert;
import org.junit.Test;

public class CIInfoSourceTest
	extends InfoSourceTest
{
	@Test
	public void testHudsonInfoSource()
	{
		final BuildServerInfoSource infoSource = new HudsonInfoSource();
		Assert.assertFalse(infoSource.isActive());
		Assert.assertNotNull(infoSource.getDescription());

		final SortedProperties props = infoSource.getInfo();
		Assert.assertNotNull(props);
		Assert.assertTrue(props.size() > 0);
		validatePropertyPrefix(props, infoSource.getPropertyPrefix());
	}

	@Test
	public void testJenkinsInfoSource()
	{
		final BuildServerInfoSource infoSource = new JenkinsInfoSource();
		Assert.assertFalse(infoSource.isActive());
		Assert.assertNotNull(infoSource.getDescription());

		final SortedProperties props = infoSource.getInfo();
		Assert.assertNotNull(props);
		Assert.assertTrue(props.size() > 0);
		validatePropertyPrefix(props, infoSource.getPropertyPrefix());
	}

	@Test
	public void testTeamCityInfoSource()
	{
		final BuildServerInfoSource infoSource = new TeamCityInfoSource();
		Assert.assertFalse(infoSource.isActive());
		Assert.assertNotNull(infoSource.getDescription());

		final SortedProperties props = infoSource.getInfo();
		Assert.assertNotNull(props);
		Assert.assertTrue(props.size() > 0);
		validatePropertyPrefix(props, infoSource.getPropertyPrefix());
	}
}
