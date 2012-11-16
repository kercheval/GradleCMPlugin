package org.kercheval.gradle.buildvcs;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.kercheval.gradle.info.GradleInfoSource;

public class BuildVCSPlugin
	implements Plugin<Project>
{
	public static final String VCS_TASK_NAME = "buildvcs";

	@Override
	public void apply(final Project project)
	{
		//
		// Create the actual task that will be executed
		//
		final Task task = project.getTasks().add(VCS_TASK_NAME, BuildVCSTask.class);
		task.setDescription("Create a VCS reference object for use in other tasks");
		task.setGroup(GradleInfoSource.PLUGIN_GROUP_NAME);
	}
}
