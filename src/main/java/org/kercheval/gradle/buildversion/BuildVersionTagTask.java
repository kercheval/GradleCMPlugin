package org.kercheval.gradle.buildversion;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class BuildVersionTagTask extends DefaultTask {
    @TaskAction
    public void doTask() {

        // This should do the following
        // - Verify the project.version is of type BuildVersion
        // - Increment project.version and write a vcs tag
        // Should the tag be pushed?  Probably not...
        getProject().getLogger().error("Executed Tag Task");
    }
}
