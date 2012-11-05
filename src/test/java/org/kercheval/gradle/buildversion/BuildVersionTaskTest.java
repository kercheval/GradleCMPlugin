package org.kercheval.gradle.buildversion;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.plugins.DefaultObjectConfigurationAction;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Assert;
import org.junit.Test;

import org.kercheval.gradle.buildinfo.BuildInfoPluginTest;

import java.io.File;

import java.text.ParseException;

import java.util.HashMap;
import java.util.Map;

public class BuildVersionTaskTest {
    @Test
    public void testBuildVersionTask() throws ParseException {
        final Project project =
            ProjectBuilder.builder().withProjectDir(new File(BuildInfoPluginTest.JUNIT_FILE_LOCATION)).build();
        final BuildVersionTask task = getTask(project);

        Assert.assertNotNull(task);
        task.doTask();
        Assert.assertTrue(project.getVersion() instanceof BuildVersion);
        System.out.println(project.getVersion());
        task.setVersion(new BuildVersion("'%M%.%m%-%d%-%t%'", null));
        task.doTask();
        Assert.assertTrue(project.getVersion() instanceof BuildVersion);
        System.out.println(project.getVersion());
        Assert.assertEquals(0, ((BuildVersion) project.getVersion()).getMajor());
        Assert.assertEquals(3, ((BuildVersion) project.getVersion()).getMinor());
    }

    private BuildVersionTask getTask(final Project project) {
        project.apply(new Closure<DefaultObjectConfigurationAction>(project, project) {
            @SuppressWarnings("unused")
            public Object doCall(final DefaultObjectConfigurationAction pluginAction) {
                pluginAction.plugin("buildversion");

                return pluginAction;
            }
        });

        final Map<String, Task> tasknameMap = new HashMap<String, Task>();

        for (final Task task : project.getAllTasks(false).get(project)) {
            System.out.println(task.getName());
            tasknameMap.put(task.getName(), task);
        }

        final BuildVersionTask task = (BuildVersionTask) tasknameMap.get("buildversion");

        return task;
    }
}
