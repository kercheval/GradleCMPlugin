package org.kercheval.gradle.buildversion;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.execution.TaskExecutionGraphListener;

public class BuildVersionPlugin implements Plugin<Project> {
    static final String MAIN_TASK_NAME = "buildVersion";
    static final String UPDATE_MINOR_TASK_NAME = "buildVersionUpdateMinor";
    static final String UPDATE_MAJOR_TASK_NAME = "buildVersionUpdateMajor";

    @Override
    public void apply(final Project project) {

        //
        // Add a listener to obtain the current version information from the system.
        // This is done here to ensure all configuration parameters have been written.
        //
        project.getGradle().getTaskGraph().addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
            @Override
            public void graphPopulated(final TaskExecutionGraph graph) {}
        });

        //
        // Create the actual task that will be executed
        //
        project.getTasks().add(MAIN_TASK_NAME, BuildVersionTask.class);
        project.getTasks().add(UPDATE_MINOR_TASK_NAME, BuildVersionUpdateMinorTask.class);
        project.getTasks().add(UPDATE_MAJOR_TASK_NAME, BuildVersionUpdateMajorTask.class);
    }
}
