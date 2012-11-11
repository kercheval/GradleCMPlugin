package org.kercheval.gradle.gradlecm;

import java.util.LinkedHashMap;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GradleCMPlugin
	implements Plugin<Project>
{
	public static final String BUILD_INFO_PLUGIN = "buildinfo";
	public static final String BUILD_VERSION_PLUGIN = "buildversion";
	public static final String BUILD_RELEASE_PLUGIN = "buildrelease";

	@Override
	public void apply(final Project project)
	{
		//
		// This plugin is a simple container to pull in all individual plugins
		//
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
