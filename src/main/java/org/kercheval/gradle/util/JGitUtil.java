package org.kercheval.gradle.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

public class JGitUtil {
    private final File gitBaseDir;
    Project project;

    public JGitUtil(Project project, File gitBaseDir) {
        this.gitBaseDir = gitBaseDir;
        this.project = project;
    }

    //
    // The info supplied here relies on the JGit library to interact with the
    // repository.  Get the environment, the latest branch commit, the origin
    // and the current file status for the workspace.
    //
    public SortedProperties getGitInfo() {
        SortedProperties props = new SortedProperties();
        Repository repository = null;

        try {
            repository = new RepositoryBuilder().readEnvironment().findGitDir(gitBaseDir).build();
            props.addProperty("git.basedir", repository.getDirectory().getCanonicalPath());
            props.addProperty("git.branch", repository.getBranch());

            ObjectId head = repository.resolve("HEAD");

            props.addProperty("git.last.commit", head.getName());

            Config config = repository.getConfig();

            props.addProperty("git.user.name", config.getString("user", null, "name"));
            props.addProperty("git.user.email", config.getString("user", null, "email"));
            props.addProperty("git.remote.origin", config.getString("remote", "origin", "url"));

            try {
                Status status = new Git(repository).status().call();

                props.addProperty("git.workspace.clean", Boolean.toString(status.isClean()));
                props.addProperty("git.workspace.files.added", status.getAdded().toString());
                props.addProperty("git.workspace.files.changed", status.getChanged().toString());
                props.addProperty("git.workspace.files.missing", status.getMissing().toString());
                props.addProperty("git.workspace.files.removed", status.getRemoved().toString());
                props.addProperty("git.workspace.files.untracked", status.getUntracked().toString());
                props.addProperty("git.workspace.files.conflicting", status.getConflicting().toString());
                props.addProperty("git.workspace.files.modified", status.getModified().toString());
            } catch (NoWorkTreeException e) {
                project.getLogger().error(e.getMessage());
            } catch (GitAPIException e) {
                project.getLogger().error(e.getMessage());
            }
        } catch (IOException e) {
            project.getLogger().error(e.getMessage());
        } finally {
            if (null != repository) {
                repository.close();
            }
        }

        return props;
    }
}
