package org.kercheval.gradle.buildinfo;

import java.io.*;
import java.util.*;

import org.gradle.api.*;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.*;
import org.kercheval.gradle.gradlecm.GradleCMPlugin;

public class BuildInfoPluginTest {
    private static final String JUNIT_FILE_LOCATION = "./build/junit_temp/infoplugin";

    private static final String JUNIT_FILE_NAME =
            JUNIT_FILE_LOCATION + "/build/" + BuildInfoTask.DEFAULT_FILENAME;

    private BuildInfoTask getTask(final Project project) {
        project.apply(new LinkedHashMap<String, Class>() {
            {
                put("plugin", GradleCMPlugin.BUILD_INFO_PLUGIN);
            }
        });

        final Map<String, Task> tasknameMap = new HashMap<String, Task>();

        for (final Task task : project.getAllTasks(false).get(project)) {
            tasknameMap.put(task.getName(), task);
        }

        final BuildInfoTask task = (BuildInfoTask) tasknameMap.get(BuildInfoPlugin.INFO_TASK_NAME);

        return task;
    }

    private void resetDefaultTaskValues(final Project project, final BuildInfoTask task)
            throws IOException {
        task.setAutowrite(BuildInfoTask.DEFAULT_AUTOWRITE);
        task.setFilename(BuildInfoTask.DEFAULT_FILENAME);
        task.setTaskmap(BuildInfoTask.DEFAULT_TASKMAP);
        task.setFiledir(((File) project.getProperties().get("buildDir")).getCanonicalPath());
        task.setCustominfo(new HashMap<String, Object>());
    }

    @Test
    public void testBuildInfoTask() throws FileNotFoundException, IOException {
        final Project project =
                ProjectBuilder.builder().withProjectDir(new File(JUNIT_FILE_LOCATION)).build();
        final BuildInfoTask task = getTask(project);

        final HashMap<String, Object> customMap = new HashMap<String, Object>();
        customMap.put("CustomKey1", "CustomValue1");
        customMap.put("CustomKey2", "CustomValue2");
        customMap.put("CustomKey3", "CustomValue3");
        task.setCustominfo(customMap);

        File outputFile = new File(JUNIT_FILE_NAME);

        if (outputFile.exists()) {
            outputFile.delete();
        }

        Assert.assertFalse(outputFile.exists());
        task.doTask();
        outputFile = new File(JUNIT_FILE_NAME);
        Assert.assertTrue(outputFile.exists());

        Properties props = new Properties();
        props.load(new FileInputStream(outputFile));

        Assert.assertFalse(props.isEmpty());

        //
        // Validate expected sources
        //
        Assert.assertTrue(props.containsKey("custom.info.CustomKey2"));
        Assert.assertTrue(props.containsKey("machine.hostname"));
        Assert.assertTrue(props.containsKey("gradle.rootdir"));

        //
        // Assuming this build is NOT in CI... :p
        //
        Assert.assertFalse(props.containsKey("ci.hudson.HUDSON_SERVER_COOKIE"));
        Assert.assertFalse(props.containsKey("ci.jenkins.JENKINS_SERVER_COOKIE"));
        Assert.assertFalse(props.containsKey("ci.teamcity.TEAMCITY_VERSION"));
        Assert.assertTrue(props.containsKey("vcs.type"));

        //
        // This project uses GIT
        //
        Assert.assertEquals("GIT", props.getProperty("vcs.type"));

        task.setShowgradleinfo(false);
        task.doTask();
        props = new Properties();
        props.load(new FileInputStream(outputFile));

        Assert.assertFalse(props.isEmpty());
        Assert.assertTrue(props.containsKey("custom.info.CustomKey2"));
        Assert.assertTrue(props.containsKey("machine.hostname"));
        Assert.assertTrue(props.containsKey("vcs.type"));
        Assert.assertFalse(props.containsKey("gradle.rootdir"));

        task.setShowmachineinfo(false);
        task.doTask();
        props = new Properties();
        props.load(new FileInputStream(outputFile));

        Assert.assertFalse(props.isEmpty());
        Assert.assertTrue(props.containsKey("custom.info.CustomKey2"));
        Assert.assertFalse(props.containsKey("machine.hostname"));
        Assert.assertTrue(props.containsKey("vcs.type"));
        Assert.assertFalse(props.containsKey("gradle.rootdir"));

        task.setShowvscinfo(false);
        task.doTask();
        props = new Properties();
        props.load(new FileInputStream(outputFile));

        Assert.assertFalse(props.isEmpty());
        Assert.assertTrue(props.containsKey("custom.info.CustomKey2"));
        Assert.assertFalse(props.containsKey("machine.hostname"));
        Assert.assertFalse(props.containsKey("vcs.type"));
        Assert.assertFalse(props.containsKey("gradle.rootdir"));
    }

    @Test
    public void testSetDefaultVariables() throws IOException {
        final Project project =
                ProjectBuilder.builder().withProjectDir(new File(JUNIT_FILE_LOCATION)).build();
        final BuildInfoTask task = getTask(project);

        //
        // Validate default values
        //
        Assert.assertSame(task.getTaskmap(), BuildInfoTask.DEFAULT_TASKMAP);
        Assert.assertEquals(task.getFiledir(),
            ((File) project.getProperties().get("buildDir")).getCanonicalPath());
        Assert.assertSame(task.getFilename(), BuildInfoTask.DEFAULT_FILENAME);
        Assert.assertTrue(task.getCustominfo().isEmpty());
        Assert.assertTrue(task.isAutowrite());
        resetDefaultTaskValues(project, task);
        Assert.assertSame(task.getTaskmap(), BuildInfoTask.DEFAULT_TASKMAP);

        final Map<String, String> taskMap = task.getTaskmap();

        Assert.assertNotNull(taskMap);
        Assert.assertEquals(3, taskMap.size());
        Assert.assertNotNull(taskMap.get("jar"));
        Assert.assertNotNull(taskMap.get("war"));
        Assert.assertNotNull(taskMap.get("ear"));
        Assert.assertNotNull(task.getFiledir());
        Assert.assertEquals(((File) project.getProperties().get("buildDir")).getCanonicalPath(),
            task.getFiledir());
        Assert.assertNotNull(task.getFilename());
        Assert.assertEquals(BuildInfoTask.DEFAULT_FILENAME, task.getFilename());
        Assert.assertTrue(task.getCustominfo().isEmpty());
        Assert.assertTrue(task.isAutowrite());

        //
        // Check individual file setting overrides
        //
        resetDefaultTaskValues(project, task);
        task.setFilename("foo");
        Assert.assertSame(task.getTaskmap(), BuildInfoTask.DEFAULT_TASKMAP);
        Assert.assertEquals("foo", task.getFilename());
        resetDefaultTaskValues(project, task);
        task.setFiledir("bar");
        Assert.assertSame(task.getTaskmap(), BuildInfoTask.DEFAULT_TASKMAP);
        Assert.assertEquals("bar", task.getFiledir());

        final HashMap<String, String> taskmap = new HashMap<String, String>();

        taskmap.put("zip", "META-INF");
        resetDefaultTaskValues(project, task);
        task.setTaskmap(taskmap);
        Assert.assertNotSame(task.getTaskmap(), BuildInfoTask.DEFAULT_TASKMAP);
        Assert.assertEquals(1, task.getTaskmap().size());
        Assert.assertNotNull(task.getTaskmap().get("zip"));
    }
}
