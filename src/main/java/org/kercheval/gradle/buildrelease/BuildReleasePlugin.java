package org.kercheval.gradle.buildrelease;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BuildReleasePlugin
	implements Plugin<Project>
{
	static final String INIT_TASK_NAME = "buildreleaseinit";

	@Override
	public void apply(final Project project)
	{

		//
		// Create the actual task that will be executed
		//
		project.getTasks().add(INIT_TASK_NAME, BuildReleaseInitTask.class);
	}
}
