package org.kercheval.gradle.buildrelease;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.kercheval.gradle.util.GradleUtil;

public class BuildReleasePlugin
	implements Plugin<Project>
{
	public static final String INIT_TASK_NAME = "buildreleaseinit";
	public static final String MERGE_TASK_NAME = "buildreleasemerge";
	public static final String UPLOAD_TASK_NAME = "buildreleaseupload";

	@Override
	public void apply(final Project project)
	{

		//
		// Create the actual tasks that will be executed
		//
		Task task = project.getTasks().add(INIT_TASK_NAME, BuildReleaseInitTask.class);
		task.setDescription("Create a release branch structure supporting release code promotion and publication");
		task.setGroup(GradleUtil.PLUGIN_GROUP_NAME);
		task = project.getTasks().add(MERGE_TASK_NAME, BuildReleaseMergeTask.class);
		task.setDescription("Update the release branch with changes made to the mainline branch");
		task.setGroup(GradleUtil.PLUGIN_GROUP_NAME);
		task = project.getTasks().add(UPLOAD_TASK_NAME, BuildReleaseUploadTask.class);
		task.setDescription("Upload artifacts from the release branch and create a release tag");
		task.setGroup(GradleUtil.PLUGIN_GROUP_NAME);
	}
}
