package org.kercheval.gradle.util;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.Task;

public class GradleUtil
{
	public static final String PLUGIN_GROUP_NAME = "Gradle CM (kercheval.org)";

	Project project;

	public GradleUtil(final Project project)
	{
		this.project = project;
	}

	//
	// Only few gradle specific properties obtained here. This should
	// normally be called after the task graph has been generated to ensure
	// the evaluation stage variable changes have been completed
	//
	public SortedProperties getGradleInfo()
	{
		final Map<String, ?> gradleProps = project.getProperties();
		final SortedProperties props = new SortedProperties();

		props.addProperty("gradle.buildfile", gradleProps.get("buildFile"));
		props.addProperty("gradle.rootdir", gradleProps.get("rootDir"));
		props.addProperty("gradle.projectdir", gradleProps.get("projectDir"));
		props.addProperty("gradle.description", gradleProps.get("description"));

		return props;
	}

	public Task getTask(final String taskname)
	{
		final Map<String, Task> tasknameMap = new HashMap<String, Task>();

		for (final Task task : project.getAllTasks(false).get(project))
		{
			tasknameMap.put(task.getName(), task);
		}

		return tasknameMap.get(taskname);
	}
}
