package org.kercheval.gradle.buildinfo;

import groovy.lang.Closure;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.plugins.DefaultObjectConfigurationAction;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

public class BuildInfoPluginTest {
    public static final String JUNIT_FILE_LOCATION = "./build/JUNIT_TEMP";
    public static final String JUNIT_FILE_NAME = "./build/JUNIT_TEMP/build/"
                                                 + BuildInfoTask.DEFAULT_BUILDINFO_PROPERTIES;

    @Test
    public void testBuildInfoTask() {
        final Project project = ProjectBuilder.builder().withProjectDir(new File(JUNIT_FILE_LOCATION)).build();
        final BuildInfoTask task = getTask(project);
        File outputFile = new File(JUNIT_FILE_NAME);

        if (outputFile.exists()) {
            outputFile.delete();
        }

        Assert.assertFalse(outputFile.exists());
        task.setDefaultVariables(project);
        task.doTask();
        outputFile = new File(JUNIT_FILE_NAME);
        Assert.assertTrue(outputFile.exists());
    }

    private BuildInfoTask getTask(final Project project) {
        project.apply(new Closure<DefaultObjectConfigurationAction>(project, project) {
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

        return task;
    }

    @Test
    public void testSetDefaultVariables() throws IOException {
        final Project project = ProjectBuilder.builder().withProjectDir(new File(JUNIT_FILE_LOCATION)).build();
        final BuildInfoTask task = getTask(project);

        //
        // Validate default values
        //
        Assert.assertNull(task.getTaskmap());
        Assert.assertNull(task.getFiledir());
        Assert.assertNull(task.getFilename());
        Assert.assertNull(task.getCustominfo());
        Assert.assertTrue(task.isAutowrite());
        resetDefaultTaskValues(task);
        Assert.assertFalse(task.setDefaultVariables(project));

        final Map<String, String> taskMap = task.getTaskmap();

        Assert.assertNotNull(taskMap);
        Assert.assertEquals(3, taskMap.size());
        Assert.assertNotNull(taskMap.get("jar"));
        Assert.assertNotNull(taskMap.get("war"));
        Assert.assertNotNull(taskMap.get("ear"));
        Assert.assertNotNull(task.getFiledir());
        Assert.assertEquals(((File) project.getProperties().get("buildDir")).getCanonicalPath(), task.getFiledir());
        Assert.assertNotNull(task.getFilename());
        Assert.assertEquals(BuildInfoTask.DEFAULT_BUILDINFO_PROPERTIES, task.getFilename());
        Assert.assertNull(task.getCustominfo());
        Assert.assertTrue(task.isAutowrite());

        //
        // Check individual file setting overrides
        //
        resetDefaultTaskValues(task);
        task.setFilename("foo");
        Assert.assertFalse(task.setDefaultVariables(project));
        Assert.assertEquals("foo", task.getFilename());
        resetDefaultTaskValues(task);
        task.setFiledir("bar");
        Assert.assertFalse(task.setDefaultVariables(project));
        Assert.assertEquals("bar", task.getFiledir());

        final HashMap<String, String> taskmap = new HashMap<String, String>();

        taskmap.put("zip", "META-INF");
        resetDefaultTaskValues(task);
        task.setTaskmap(taskmap);
        Assert.assertTrue(task.setDefaultVariables(project));
        Assert.assertEquals(1, task.getTaskmap().size());
        Assert.assertNotNull(task.getTaskmap().get("zip"));
    }

    private void resetDefaultTaskValues(final BuildInfoTask task) {
        task.setAutowrite(true);
        task.setFilename(null);
        task.setFiledir(null);
        task.setTaskmap(null);
        task.setCustominfo(null);
    }
}
