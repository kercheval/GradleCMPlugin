package org.kercheval.gradle.buildrelease;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.kercheval.gradle.util.GradleUtil;

public class BuildReleaseTask
	extends DefaultTask
{
	public BuildReleaseTask()
	{
		final Project project = getProject();
		final BuildReleaseInitTask initTask = (BuildReleaseInitTask) new GradleUtil(project)
			.getTask(BuildReleasePlugin.INIT_TASK_NAME);
		dependsOn(":" + BuildReleasePlugin.MERGE_TASK_NAME);
		dependsOn(":" + initTask.getUploadtask());
	}

	@TaskAction
	public void doTask()
	{}
}
