package org.kercheval.gradle.info;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.Task;

public class GradleInfoSource
	implements InfoSource
{
	public static final String PLUGIN_GROUP_NAME = "Gradle CM (kercheval.org)";

	Project project;

	public GradleInfoSource(final Project project)
	{
		this.project = project;
	}

	@Override
	public String getDescription()
	{
		return "Gradle (http://www.gradle.org/) build system environment information";
	}

	//
	// Only few gradle specific properties obtained here. This should
	// normally be called after the task graph has been generated to ensure
	// the evaluation stage variable changes have been completed
	//
	@Override
	public SortedProperties getInfo()
	{
		final Map<String, ?> gradleProps = project.getProperties();
		final SortedProperties props = new SortedProperties();

		props.addProperty(getPropertyPrefix() + ".buildfile", gradleProps.get("buildFile"));
		props.addProperty(getPropertyPrefix() + ".rootdir", gradleProps.get("rootDir"));
		props.addProperty(getPropertyPrefix() + ".projectdir", gradleProps.get("projectDir"));
		props.addProperty(getPropertyPrefix() + ".description", gradleProps.get("description"));

		return props;
	}

	@Override
	public String getPropertyPrefix()
	{
		return "gradle";
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

	@Override
	public boolean isActive()
	{
		return true;
	}
}
