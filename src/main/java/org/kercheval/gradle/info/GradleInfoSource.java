package org.kercheval.gradle.info;

import java.util.HashMap;
import java.util.List;
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

	//
	// This method is called for tasks which depend on dynamic
	// tasks (such as uploadArchives).
	//
	public boolean enableTask(final String taskName)
	{
		boolean rVal = true;

		//
		// TODO 11/14/2012 This odd little cantrip is necessary to avoid a concurrent
		// modification exception for the 'tasks' task. This was logged as
		// http://issues.gradle.org//browse/GRADLE-2023. This workaround can be removed
		// as soon as the system is fixed in this area
		//
		final List<String> taskList = project.getGradle().getStartParameter().getTaskNames();
		if (taskList.contains("tasks") || taskList.contains("task"))
		{
			project
				.getLogger()
				.info(
					"The 'tasks' target has been specified.  The '"
						+ taskName
						+ "' task has been disabled to avoid gradle internal problems.  See http://issues.gradle.org//browse/GRADLE-2023 for details.");
			rVal = false;
		}

		return rVal;
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
