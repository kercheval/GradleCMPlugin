package org.kercheval.gradle.buildinfo;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BuildInfoPlugin implements Plugin<Project> {
    static final String TASK_NAME = "buildinfo";

    @Override
    public void apply(Project project) {

        //
        // Create the actual task that will be executed
        //
        project.getTasks().add(TASK_NAME, BuildInfoTask.class);
    }
}
