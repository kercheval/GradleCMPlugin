package org.kercheval.gradle.buildinfo;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.plugins.DefaultObjectConfigurationAction;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import java.util.HashMap;
import java.util.Map;

public class BuildInfoPluginTest {
    public static final String JUNIT_FILE_LOCATION = "./build/JUNIT_TEMP";

    @Test
    public void testBuildInfo() {
        final Project project = ProjectBuilder.builder().withProjectDir(new File(JUNIT_FILE_LOCATION)).build();

        project.apply(new Closure<BuildInfoPlugin>(project, project) {
            @SuppressWarnings("unused")
            public Object doCall(final DefaultObjectConfigurationAction pluginAction) {
                pluginAction.plugin("buildinfo");

                return pluginAction;
            }
        });

        final Map<String, Task> tasknameMap = new HashMap<String, Task>();

        for (final Task task : project.getAllTasks(false).get(project)) {
            tasknameMap.put(task.getName(), task);
        }

        final BuildInfoTask task = (BuildInfoTask) tasknameMap.get("buildinfo");
        File outputFile = new File(JUNIT_FILE_LOCATION + "/build/buildinfo.properties");

        if (outputFile.exists()) {
            outputFile.delete();
        }

        Assert.assertFalse(outputFile.exists());
        task.setDefaultVariables(project);
        task.doTask();
        outputFile = new File(JUNIT_FILE_LOCATION + "/build/buildinfo.properties");
        Assert.assertTrue(outputFile.exists());
    }
}
