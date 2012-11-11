package org.kercheval.gradle.buildinfo;

import java.util.LinkedHashMap;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.kercheval.gradle.gradlecm.GradleCMPlugin;
import org.kercheval.gradle.util.GradleUtil;

public class BuildInfoPlugin
	implements Plugin<Project>
{
	public static final String INFO_TASK_NAME = "buildinfo";

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
		final Task task = project.getTasks().add(INFO_TASK_NAME, BuildInfoTask.class);
		task.setDescription("Create a build information file to be included in built artifacts");
		task.setGroup(GradleUtil.PLUGIN_GROUP_NAME);
	}
}
