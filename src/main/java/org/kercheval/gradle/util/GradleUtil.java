package org.kercheval.gradle.util;

import org.gradle.api.Project;

import java.util.Map;

public class GradleUtil
{
	Project project;

	public GradleUtil(final Project project)
	{
		this.project = project;
	}

	//
	// Only few gradle specific properties obtained here. This should
	// normally be called after the task graph has been generated to ensure
	// the evaluation stage variable changes have been completed
	//
	public SortedProperties getGradleInfo()
	{
		final Map<String, ?> gradleProps = project.getProperties();
		final SortedProperties props = new SortedProperties();

		props.addProperty("gradle.buildfile", gradleProps.get("buildFile"));
		props.addProperty("gradle.rootdir", gradleProps.get("rootDir"));
		props.addProperty("gradle.projectdir", gradleProps.get("projectDir"));
		props.addProperty("gradle.description", gradleProps.get("description"));

		return props;
	}
}
