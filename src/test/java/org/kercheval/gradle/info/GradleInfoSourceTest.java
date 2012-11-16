package org.kercheval.gradle.info;

import java.util.LinkedHashMap;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.kercheval.gradle.gradlecm.GradleCMPlugin;

public class GradleInfoSourceTest
	extends InfoSourceTest
{
	@Test
	public void testInfoSource()
	{
		final Project project = ProjectBuilder.builder().build();
		final GradleInfoSource infoSource = new GradleInfoSource(project);

		Assert.assertNull(infoSource.getTask("buildinfo"));

		project.apply(new LinkedHashMap<String, String>()
		{
			{
				put("plugin", GradleCMPlugin.BUILD_INFO_PLUGIN);
			}
		});

		Assert.assertTrue(infoSource.isActive());
		Assert.assertNotNull(infoSource.getDescription());

		final SortedProperties props = infoSource.getInfo();
		Assert.assertNotNull(props);
		Assert.assertTrue(props.size() > 0);
		validatePropertyPrefix(props, infoSource.getPropertyPrefix());

		Assert.assertTrue(infoSource.enableTask("hello"));
		project.getGradle().getStartParameter().getTaskNames().add("tasks");
		Assert.assertFalse(infoSource.enableTask("hello"));
		project.getGradle().getStartParameter().getTaskNames().remove("tasks");
		project.getGradle().getStartParameter().getTaskNames().add("task");
		Assert.assertFalse(infoSource.enableTask("hello"));

		Assert.assertNotNull(infoSource.getTask("buildinfo"));
	}
}
