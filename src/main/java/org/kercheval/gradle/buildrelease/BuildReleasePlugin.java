package org.kercheval.gradle.buildrelease;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BuildReleasePlugin
	implements Plugin<Project>
{
	static final String INIT_TASK_NAME = "buildreleaseinit";
	static final String PUSH_TASK_NAME = "buildreleasepush";

	@Override
	public void apply(final Project project)
	{

		//
		// Create the actual tasks that will be executed
		//
		project.getTasks().add(INIT_TASK_NAME, BuildReleaseInitTask.class);
		project.getTasks().add(PUSH_TASK_NAME, BuildReleasePushTask.class);
	}
}
