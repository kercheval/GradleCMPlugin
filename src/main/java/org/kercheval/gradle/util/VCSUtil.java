package org.kercheval.gradle.util;

import org.gradle.api.Project;

import org.kercheval.gradle.vcs.VCSAccessFactory;

import java.io.File;

public class VCSUtil {
    private final File srcBaseDir;
    Project project;

    public VCSUtil(final Project project, final File srcBaseDir) {
        this.srcBaseDir = srcBaseDir;
        this.project = project;
    }

    //
    // The info supplied here relies on the JGit library to interact with the
    // repository.  Get the environment, the latest branch commit, the origin
    // and the current file status for the workspace.
    //
    public SortedProperties getVCSInfo() {
        return VCSAccessFactory.getCurrentVCS(srcBaseDir, project.getLogger()).getVCSInfo();
    }
}
