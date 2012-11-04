package org.kercheval.gradle.buildversion;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.execution.TaskExecutionGraphListener;
import org.gradle.api.tasks.TaskAction;

import org.kercheval.gradle.vcs.IVCSAccess;
import org.kercheval.gradle.vcs.VCSAccessFactory;
import org.kercheval.gradle.vcs.VCSTag;

import java.io.File;

import java.text.ParseException;

import java.util.List;
import java.util.Map;

public class BuildVersionTask extends DefaultTask {
    private boolean autowrite = true;
    private BuildVersion version = new BuildVersion(null, 0, 0, 0, null);

    public BuildVersionTask() {

        //
        // Add a listener to obtain the current version information from the system.
        // This is done here to ensure all configuration parameters have been written.
        //
        final Project project = getProject();

        project.getGradle().getTaskGraph().addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
            @Override
            public void graphPopulated(final TaskExecutionGraph graph) {

                //
                // Set the version automagically unless we have explicitly been
                // told not to set the version
                //
                if (isAutowrite()) {
                    doTask();
                }
            }
        });
    }

    public BuildVersion getVersion() {
        return version;
    }

    public void setVersion(final BuildVersion version) {
        this.version = version;
    }

    public void setAutowrite(final boolean autowrite) {
        this.autowrite = autowrite;
    }

    protected boolean isAutowrite() {
        return autowrite;
    }

    @TaskAction
    public void doTask() {
        final Project project = getProject();

        //
        // Get the version from VCS and set the project version to our shiny new
        // version object.  The project version is used by most other tasks during
        // execution for naming purposes.
        //
        setVersion(getVersionFromVCS(project));
        project.setVersion(getVersion());
    }

    private BuildVersion getVersionFromVCS(final Project project) {
        BuildVersion rVal = getVersion();
        final Map<String, ?> props = project.getProperties();

        //
        // Get the filtered list of tags from VCS and iterate to find the newest one.
        //
        final IVCSAccess vcs = VCSAccessFactory.getCurrentVCS((File) props.get("rootDir"), project.getLogger());
        final List<VCSTag> tagList = vcs.getTags(getVersion().getCandidatePattern());
        VCSTag foundTag = null;

        for (final VCSTag tag : tagList) {
            if (null == foundTag) {
                foundTag = tag;
            } else {
                if (foundTag.getCommitDate().before(tag.getCommitDate())) {
                    foundTag = tag;
                }
            }
        }

        //
        // If we found a matching tag, generate the build version based on that tag name and return
        //
        if (null != foundTag) {
            try {
                rVal = new BuildVersion(rVal.getPattern(), rVal.getCandidatePattern(), foundTag.getName());
            } catch (final ParseException e) {
                project.getLogger().error("Unable to generate version from tag '" + foundTag + "': " + e.getMessage());
            }
        }

        return rVal;
    }
}
