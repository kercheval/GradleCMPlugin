package org.kercheval.gradle.buildinfo;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.execution.TaskExecutionGraphListener;
import org.gradle.api.tasks.TaskAction;

import org.kercheval.gradle.util.GradleUtil;
import org.kercheval.gradle.util.JGitUtil;
import org.kercheval.gradle.util.JenkinsUtil;
import org.kercheval.gradle.util.MachineUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Date;
import java.util.Map;

public class BuildInfoTask extends DefaultTask {
    static private String EOL = System.getProperty("line.separator");

    //
    // If true, the buildinfo file will be written automatically at
    // the beginning of the task evaluation phase for the project.
    //
    private boolean autowrite = true;

    //
    // This is the file name to use for info file.  The default value
    // for this filename is buildinfo.properties
    //
    private String filename;

    //
    // This directory represents the path used for the file written.
    // This value will default to ${buildDir}
    //
    private String filedir;

    public BuildInfoTask() {

        //
        // Add a listener to automatically write the build info file as soon
        // as the task graph is completed.  This ensures that all tasks have
        // completed their configuration phase and all variable updates have
        // been completed for the purposes of this plugin.
        //
        getProject().getGradle().getTaskGraph().addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
            @Override
            public void graphPopulated(TaskExecutionGraph graph) {
                Project project = getProject();
                Map<String, ?> props = project.getProperties();

                if (getFiledir() == null) {

                    //
                    // filedir was not set in gradle file, so set to default value
                    //
                    try {
                        setFiledir(((File) props.get("buildDir")).getCanonicalPath());
                    } catch (IOException e) {
                        project.getLogger().error(e.getMessage());
                    }
                }

                if (getFilename() == null) {

                    //
                    // Set default filename
                    //
                    setFilename("buildinfo.properties");
                }

                if (isAutowrite()) {
                    doTask();
                }
            }
        });
    }

    public boolean isAutowrite() {
        return autowrite;
    }

    public void setAutowrite(boolean autowrite) {
        this.autowrite = autowrite;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFiledir() {
        return filedir;
    }

    public void setFiledir(String filedir) {
        this.filedir = filedir;
    }

    @TaskAction
    public void doTask() {

        //
        // Obtain the project properties
        //
        Project project = this.getProject();
        Map<String, ?> props = project.getProperties();

        try {

            //
            // Use mkdir support to ensure the directory exists
            //
            File buildDirFile = new File(getFiledir());

            project.mkdir(buildDirFile);

            //
            // Create the file spec for our file writer
            //
            StringBuilder sb = new StringBuilder();

            sb.append(buildDirFile.getCanonicalPath());
            sb.append("/");
            sb.append(getFilename());

            BufferedWriter out = new BufferedWriter(new FileWriter(sb.toString()));

            //
            // Write out the header
            //
            out.write("#");
            out.write(EOL);
            out.write("# Build Info Created by ");
            out.write(getName());
            out.write(" on ");
            out.write(new Date().toString());
            out.write(EOL);
            out.write("#");
            out.write(EOL);
            out.write("# Tasks executing in this build");
            out.write(EOL);

            //
            // Include all the tasks that are to be executed in this build.
            // These are scheduled tasks, there is no guarantee that the tasks
            // will actually be run or will have succeeded if run
            //
            for (Task task : project.getGradle().getTaskGraph().getAllTasks()) {
                out.write("#   ");
                out.write(task.getPath());
                out.write(EOL);
            }

            out.write("#");
            out.write(EOL);
            out.write("#");
            out.write(EOL);
            out.write(EOL);

            //
            // Grab properties from our various information sources
            //
            new MachineUtil(project).getMachineInfo().store(out, "Machine Info");
            out.write(EOL);
            out.write(EOL);
            new GradleUtil(project).getGradleInfo().store(out, "Gradle Info");
            out.write(EOL);
            out.write(EOL);
            new JGitUtil(project, (File) props.get("rootDir")).getGitInfo().store(out, "Git Info");
            out.write(EOL);
            out.write(EOL);
            new JenkinsUtil().getJenkinsInfo().store(out, "Jenkins Info");
            out.close();
        } catch (IOException e) {
            project.getLogger().error(e.getMessage());
        }
    }
}
