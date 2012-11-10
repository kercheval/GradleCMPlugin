package org.kercheval.gradle.buildrelease;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.kercheval.gradle.util.GradleUtil;

public class BuildReleasePlugin
	implements Plugin<Project>
{
	static final String INIT_TASK_NAME = "buildreleaseinit";
	static final String PUSH_TASK_NAME = "buildreleasemerge";

	@Override
	public void apply(final Project project)
	{

		//
		// Create the actual tasks that will be executed
		//
		Task task = project.getTasks().add(INIT_TASK_NAME, BuildReleaseInitTask.class);
		task.setDescription("Create a release branch structure supporting release code promotion and publication");
		task.setGroup(GradleUtil.PLUGIN_GROUP_NAME);
		task = project.getTasks().add(PUSH_TASK_NAME, BuildReleaseMergeTask.class);
		task.setDescription("Update the release branch with changes made to the mainline branch");
		task.setGroup(GradleUtil.PLUGIN_GROUP_NAME);
	}
}
