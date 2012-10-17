package org.kercheval.gradle.buildinfo;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import org.kercheval.gradle.util.GradleUtil;
import org.kercheval.gradle.util.JGitUtil;
import org.kercheval.gradle.util.JenkinsUtil;
import org.kercheval.gradle.util.MachineUtil;
import org.kercheval.gradle.util.SortedProperties;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Date;
import java.util.Map;

public class BuildInfoTask extends DefaultTask {
    static String EOL = System.getProperty("line.separator");
    SortedProperties machineProps;

    public BuildInfoTask() {

        //
        // Get the machine info on task init
        //
        machineProps = new MachineUtil().getMachineInfo();
    }

    @TaskAction
    public void doTask() {
        Project project = this.getProject();
        Map<String, ?> props = project.getProperties();

        try {
            File buildDirFile = (File) props.get("buildDir");
            StringBuilder sb = new StringBuilder(buildDirFile.getCanonicalPath());

            sb.append("/").append("/buildinfo.properties");
            project.mkdir(buildDirFile);

            BufferedWriter out = new BufferedWriter(new FileWriter(sb.toString()));

            out.write("#");
            out.write(EOL);
            out.write("# Build Info Created by ");
            out.write(getName());
            out.write(" on ");
            out.write(new Date().toString());
            out.write(EOL);
            out.write("#");
            out.write(EOL);
            out.write(EOL);
            machineProps.store(out, "Machine Info");
            out.write(EOL);
            out.write(EOL);
            new GradleUtil(project).getGradleInfo().store(out, "Gradle Info");
            out.write(EOL);
            out.write(EOL);
            new JGitUtil((File) props.get("rootDir")).getGitInfo().store(out, "Git Info");
            out.write(EOL);
            out.write(EOL);
            new JenkinsUtil().getJenkinsInfo().store(out, "Jenkins Info");
            out.close();
        } catch (IOException e) {
            project.getLogger().error(e.getMessage());
        }
    }
}
