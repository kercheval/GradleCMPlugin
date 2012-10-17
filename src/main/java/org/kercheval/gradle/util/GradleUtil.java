package org.kercheval.gradle.util;

import org.gradle.api.Project;

import java.util.Map;

public class GradleUtil {
    Project project;

    public GradleUtil(Project project) {
        this.project = project;
    }

    public SortedProperties getGradleInfo() {
        Map<String, ?> gradleProps = project.getProperties();
        SortedProperties props = new SortedProperties();

        props.addProperty("gradle.buildfile", gradleProps.get("buildFile"));
        props.addProperty("gradle.rootdir", gradleProps.get("rootDir"));
        props.addProperty("gradle.projectdir", gradleProps.get("projectDir"));
        props.addProperty("gradle.description", gradleProps.get("description"));

        return props;
    }
}
