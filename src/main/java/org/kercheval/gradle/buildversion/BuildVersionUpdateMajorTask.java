package org.kercheval.gradle.buildversion;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class BuildVersionUpdateMajorTask extends DefaultTask {
    @TaskAction
    public void doTask() {
        getProject().getLogger().error("Executed Major Version Update Task");
    }
}
