package org.kercheval.gradle.buildversion;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BuildVersionPlugin implements Plugin<Project> {
    static final String MAIN_TASK_NAME = "buildversion";
    static final String TAG_TASK_NAME = "buildversiontag";

    @Override
    public void apply(final Project project) {

        //
        // Create the actual task that will be executed
        //
        project.getTasks().add(MAIN_TASK_NAME, BuildVersionTask.class);
        project.getTasks().add(TAG_TASK_NAME, BuildVersionTagTask.class);
    }
}
