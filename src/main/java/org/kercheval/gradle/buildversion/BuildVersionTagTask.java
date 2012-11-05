package org.kercheval.gradle.buildversion;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import org.kercheval.gradle.vcs.IVCSAccess;
import org.kercheval.gradle.vcs.VCSAccessFactory;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSTag;

import java.io.File;

import java.util.Map;

public class BuildVersionTagTask extends DefaultTask {

    //
    // The comment is set to a string that will be placed in the comment
    // of the tag written to VCS
    //
    private String comment = "Tag created by " + this.getName();

    public BuildVersionTagTask() {
        this.dependsOn(":" + BuildVersionPlugin.MAIN_TASK_NAME);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    @TaskAction
    public void doTask() {
        if (getProject().getVersion() instanceof BuildVersion) {
            final Map<String, ?> props = getProject().getProperties();
            final IVCSAccess vcs = VCSAccessFactory.getCurrentVCS((File) props.get("rootDir"),
                                       getProject().getLogger());

            //
            // Write a tag into VCS using the current project version
            //
            try {
                vcs.setTag(new VCSTag(getProject().getVersion().toString(), comment));
            } catch (final VCSException e) {
                throw new TaskExecutionException(this, e);
            }
        } else {
            throw new TaskExecutionException(
                this,
                new IllegalStateException(
                    "Project version is not of type BuildVersion: ensure buildversion task has been run or set project version to an object of type BuildVersion."));
        }
    }
}
