package org.kercheval.gradle.buildversion;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class BuildVersionTask extends DefaultTask {
    @TaskAction
    public void doTask() {
        getProject().getLogger().error("Executed Default Version");
    }
}
