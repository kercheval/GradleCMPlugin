package org.kercheval.gradle.gradlecm;

import java.util.LinkedHashMap;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GradleCMPlugin
	implements Plugin<Project>
{
	public static final String GRADLE_CM_PLUGIN = "gradlecm";
	public static final String BUILD_INFO_PLUGIN = "buildinfo";
	public static final String BUILD_VERSION_PLUGIN = "buildversion";
	public static final String BUILD_RELEASE_PLUGIN = "buildrelease";
	public static final String BUILD_VCS_PLUGIN = "buildvcs";

	@Override
	public void apply(final Project project)
	{
		//
		// This plugin is a simple container without tasks to pull in all
		// individual plugins in the group.
		//
		project.apply(new LinkedHashMap<String, String>()
		{
			{
				put("plugin", BUILD_VCS_PLUGIN);
			}
		});
		project.apply(new LinkedHashMap<String, String>()
		{
			{
				put("plugin", BUILD_INFO_PLUGIN);
			}
		});
		project.apply(new LinkedHashMap<String, String>()
		{
			{
				put("plugin", BUILD_VERSION_PLUGIN);
			}
		});
		project.apply(new LinkedHashMap<String, String>()
		{
			{
				put("plugin", BUILD_RELEASE_PLUGIN);
			}
		});
	}
}
