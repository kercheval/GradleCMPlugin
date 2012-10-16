package org.kercheval.gradle.about;

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

import java.util.Properties;

public class JGitUtil {
    private final File gitBaseDir;

    public JGitUtil(File gitBaseDir) {
        this.gitBaseDir = gitBaseDir;
    }

    public Properties getStatus() {
        Properties props = new Properties();
        Repository repository = null;

        try {
            repository = new RepositoryBuilder().readEnvironment().findGitDir(gitBaseDir).build();
            PropertyUtil.addProperty(props, "git.basedir", repository.getDirectory().getCanonicalPath());
            PropertyUtil.addProperty(props, "git.branch", repository.getBranch());

            ObjectId head = repository.resolve("HEAD");

            PropertyUtil.addProperty(props, "git.last.commit", head.getName());

            Config config = repository.getConfig();

            PropertyUtil.addProperty(props, "git.user.name", config.getString("user", null, "name"));
            PropertyUtil.addProperty(props, "git.user.email", config.getString("user", null, "email"));
            PropertyUtil.addProperty(props, "git.remote.origin", config.getString("remote", "origin", "url"));

            try {
                Status status = new Git(repository).status().call();

                PropertyUtil.addProperty(props, "git.workspace.clean", Boolean.toString(status.isClean()));
                PropertyUtil.addProperty(props, "git.workspace.files.added", status.getAdded().toString());
                PropertyUtil.addProperty(props, "git.workspace.files.changed", status.getChanged().toString());
                PropertyUtil.addProperty(props, "git.workspace.files.missing", status.getMissing().toString());
                PropertyUtil.addProperty(props, "git.workspace.files.removed", status.getRemoved().toString());
                PropertyUtil.addProperty(props, "git.workspace.files.untracked", status.getUntracked().toString());
                PropertyUtil.addProperty(props, "git.workspace.files.conflicting", status.getConflicting().toString());
                PropertyUtil.addProperty(props, "git.workspace.files.modified", status.getModified().toString());
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
