package org.kercheval.gradle.buildversion;

import java.util.LinkedHashMap;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.kercheval.gradle.gradlecm.GradleCMPlugin;
import org.kercheval.gradle.info.GradleInfoSource;

public class BuildVersionPlugin
	implements Plugin<Project>
{
	public static final String VERSION_TASK_NAME = "buildversion";
	public static final String TAG_TASK_NAME = "buildversiontag";

	@Override
	public void apply(final Project project)
	{
		//
		// We need the VCS plugin for this
		//
		project.apply(new LinkedHashMap<String, String>()
		{
			{
				put("plugin", GradleCMPlugin.BUILD_VCS_PLUGIN);
			}
		});

		//
		// Create the actual task that will be executed
		//
		Task task = project.getTasks().create(VERSION_TASK_NAME, BuildVersionTask.class);
		task.setDescription("Determine the current project version based on VCS tags");
		task.setGroup(GradleInfoSource.PLUGIN_GROUP_NAME);

		task = project.getTasks().create(TAG_TASK_NAME, BuildVersionTagTask.class);
		task.setDescription("Create a VCS with with a name based on the current project version");
		task.setGroup(GradleInfoSource.PLUGIN_GROUP_NAME);
	}
}
