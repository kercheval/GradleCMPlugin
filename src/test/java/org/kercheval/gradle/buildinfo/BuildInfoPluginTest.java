package org.kercheval.gradle.buildinfo;

import groovy.lang.Closure;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.plugins.DefaultObjectConfigurationAction;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;

public class BuildInfoPluginTest
{
	public static final String JUNIT_FILE_LOCATION = "./build/JUNIT_TEMP";
	public static final String JUNIT_FILE_NAME = "./build/JUNIT_TEMP/build/"
		+ BuildInfoTask.DEFAULT_FILENAME;

	private BuildInfoTask getTask(final Project project)
	{
		project.apply(new Closure<DefaultObjectConfigurationAction>(project, project)
		{
			@SuppressWarnings("unused")
			public Object doCall(final DefaultObjectConfigurationAction pluginAction)
			{
				pluginAction.plugin("buildinfo");

				return pluginAction;
			}
		});

		final Map<String, Task> tasknameMap = new HashMap<String, Task>();

		for (final Task task : project.getAllTasks(false).get(project))
		{
			tasknameMap.put(task.getName(), task);
		}

		final BuildInfoTask task = (BuildInfoTask) tasknameMap.get("buildinfo");

		return task;
	}

	private void resetDefaultTaskValues(final Project project, final BuildInfoTask task)
		throws IOException
	{
		task.setAutowrite(BuildInfoTask.DEFAULT_AUTOWRITE);
		task.setFilename(BuildInfoTask.DEFAULT_FILENAME);
		task.setTaskmap(BuildInfoTask.DEFAULT_TASKMAP);
		task.setFiledir(((File) project.getProperties().get("buildDir")).getCanonicalPath());
		task.setCustominfo(new HashMap<String, Object>());
	}

	@Test
	public void testBuildInfoTask()
	{
		final Project project = ProjectBuilder.builder()
			.withProjectDir(new File(JUNIT_FILE_LOCATION)).build();
		final BuildInfoTask task = getTask(project);
		File outputFile = new File(JUNIT_FILE_NAME);

		if (outputFile.exists())
		{
			outputFile.delete();
		}

		Assert.assertFalse(outputFile.exists());
		task.doTask();
		outputFile = new File(JUNIT_FILE_NAME);
		Assert.assertTrue(outputFile.exists());
	}

	@Test
	public void testSetDefaultVariables()
		throws IOException
	{
		final Project project = ProjectBuilder.builder()
			.withProjectDir(new File(JUNIT_FILE_LOCATION)).build();
		final BuildInfoTask task = getTask(project);

		//
		// Validate default values
		//
		Assert.assertSame(task.getTaskmap(), BuildInfoTask.DEFAULT_TASKMAP);
		Assert.assertEquals(task.getFiledir(),
			((File) project.getProperties().get("buildDir")).getCanonicalPath());
		Assert.assertSame(task.getFilename(), BuildInfoTask.DEFAULT_FILENAME);
		Assert.assertTrue(task.getCustominfo().isEmpty());
		Assert.assertTrue(task.isAutowrite());
		resetDefaultTaskValues(project, task);
		Assert.assertSame(task.getTaskmap(), BuildInfoTask.DEFAULT_TASKMAP);

		final Map<String, String> taskMap = task.getTaskmap();

		Assert.assertNotNull(taskMap);
		Assert.assertEquals(3, taskMap.size());
		Assert.assertNotNull(taskMap.get("jar"));
		Assert.assertNotNull(taskMap.get("war"));
		Assert.assertNotNull(taskMap.get("ear"));
		Assert.assertNotNull(task.getFiledir());
		Assert.assertEquals(((File) project.getProperties().get("buildDir")).getCanonicalPath(),
			task.getFiledir());
		Assert.assertNotNull(task.getFilename());
		Assert.assertEquals(BuildInfoTask.DEFAULT_FILENAME, task.getFilename());
		Assert.assertTrue(task.getCustominfo().isEmpty());
		Assert.assertTrue(task.isAutowrite());

		//
		// Check individual file setting overrides
		//
		resetDefaultTaskValues(project, task);
		task.setFilename("foo");
		Assert.assertSame(task.getTaskmap(), BuildInfoTask.DEFAULT_TASKMAP);
		Assert.assertEquals("foo", task.getFilename());
		resetDefaultTaskValues(project, task);
		task.setFiledir("bar");
		Assert.assertSame(task.getTaskmap(), BuildInfoTask.DEFAULT_TASKMAP);
		Assert.assertEquals("bar", task.getFiledir());

		final HashMap<String, String> taskmap = new HashMap<String, String>();

		taskmap.put("zip", "META-INF");
		resetDefaultTaskValues(project, task);
		task.setTaskmap(taskmap);
		Assert.assertNotSame(task.getTaskmap(), BuildInfoTask.DEFAULT_TASKMAP);
		Assert.assertEquals(1, task.getTaskmap().size());
		Assert.assertNotNull(task.getTaskmap().get("zip"));
	}
}
