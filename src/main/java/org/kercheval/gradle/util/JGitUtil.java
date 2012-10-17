package org.kercheval.gradle.util;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.File;
import java.io.IOException;


public class JGitUtil {
    private final File gitBaseDir;

    public JGitUtil(File gitBaseDir) {
        this.gitBaseDir = gitBaseDir;
    }

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

                // TODO: log exception here
            } catch (GitAPIException e) {

                // TODO: log exception here
            }
        } catch (IOException e) {

            // TODO: log exception here
        } finally {
            if (null != repository) {
                repository.close();
            }
        }

        return props;
    }
}
