package org.kercheval.gradle.buildversion;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.kercheval.gradle.util.GradleUtil;

public class BuildVersionPlugin
	implements Plugin<Project>
{
	static final String MAIN_TASK_NAME = "buildversion";
	static final String TAG_TASK_NAME = "buildversiontag";

	@Override
	public void apply(final Project project)
	{

		//
		// Create the actual task that will be executed
		//
		Task task = project.getTasks().add(MAIN_TASK_NAME, BuildVersionTask.class);
		task.setDescription("Determine the current project version based on VCS tags");
		task.setGroup(GradleUtil.PLUGIN_GROUP_NAME);

		task = project.getTasks().add(TAG_TASK_NAME, BuildVersionTagTask.class);
		task.setDescription("Create a VCS with with a name based on the current project version");
		task.setGroup(GradleUtil.PLUGIN_GROUP_NAME);
	}
}
