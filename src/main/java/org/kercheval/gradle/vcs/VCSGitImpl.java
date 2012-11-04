package org.kercheval.gradle.vcs;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;

import org.gradle.api.logging.Logger;

import org.kercheval.gradle.util.SortedProperties;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//
// This class implements the VCSAccess interface for GIT.
//
public class VCSGitImpl implements IVCSAccess {
    static Object singletonLock = new Object();
    static VCSGitImpl singleton = null;
    private final File srcRootDir;
    private final Logger logger;

    private VCSGitImpl(final File srcRootDir, final Logger logger) {
        this.srcRootDir = srcRootDir;
        this.logger = logger;
    }

    public static IVCSAccess getInstance(final File srcRootDir, final Logger logger) {
        synchronized (singletonLock) {
            VCSGitImpl rVal = singleton;

            if (null == rVal) {
                rVal = new VCSGitImpl(srcRootDir, logger);
            }

            return rVal;
        }
    }

    public File getSrcRootDir() {
        return srcRootDir;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public SortedProperties getInfo() {
        final SortedProperties props = new SortedProperties();
        Repository repository = null;

        try {
            repository = new RepositoryBuilder().readEnvironment().findGitDir(getSrcRootDir()).build();
            props.addProperty("git.basedir", repository.getDirectory().getCanonicalPath());
            props.addProperty("git.branch", repository.getBranch());

            final ObjectId head = repository.resolve("HEAD");

            props.addProperty("git.last.commit", head.getName());

            final Config config = repository.getConfig();

            props.addProperty("git.user.name", config.getString("user", null, "name"));
            props.addProperty("git.user.email", config.getString("user", null, "email"));
            props.addProperty("git.remote.origin", config.getString("remote", "origin", "url"));

            try {
                final Status status = new Git(repository).status().call();

                props.addProperty("git.workspace.clean", Boolean.toString(status.isClean()));
                props.addProperty("git.workspace.files.added", status.getAdded().toString());
                props.addProperty("git.workspace.files.changed", status.getChanged().toString());
                props.addProperty("git.workspace.files.missing", status.getMissing().toString());
                props.addProperty("git.workspace.files.removed", status.getRemoved().toString());
                props.addProperty("git.workspace.files.untracked", status.getUntracked().toString());
                props.addProperty("git.workspace.files.conflicting", status.getConflicting().toString());
                props.addProperty("git.workspace.files.modified", status.getModified().toString());
            } catch (final NoWorkTreeException e) {
                getLogger().error(e.getMessage());
            } catch (final GitAPIException e) {
                getLogger().error(e.getMessage());
            }
        } catch (final IOException e) {
            getLogger().error(e.getMessage());
        } finally {
            if (null != repository) {
                repository.close();
            }
        }

        return props;
    }

    @Override
    public Type getType() {
        return IVCSAccess.Type.GIT;
    }

    @Override
    public List<VCSTag> getAllTags() {
        final List<VCSTag> rVal = new ArrayList<VCSTag>();
        Repository repository = null;

        try {
            repository = new RepositoryBuilder().readEnvironment().findGitDir(getSrcRootDir()).build();

            final Map<String, Ref> tags = repository.getTags();

            for (final String name : tags.keySet()) {
                final Ref ref = tags.get(name);
                final RevWalk revWalk = new RevWalk(repository);

                try {
                    final RevTag revTag = revWalk.parseTag(ref.getObjectId());

                    if (null != revTag) {
                        final PersonIdent ident = revTag.getTaggerIdent();

                        if (null != ident) {
                            rVal.add(new VCSTag(revTag.getTagName(), revTag.getName(), revTag.getFullMessage(),
                                                ident.getName(), ident.getEmailAddress(), ident.getWhen()));
                        }
                    }
                } finally {
                    if (null != revWalk) {
                        revWalk.dispose();
                    }
                }
            }
        } catch (final IOException e) {
            getLogger().error(e.getMessage());
        } finally {
            if (null != repository) {
                repository.close();
            }
        }

        return rVal;
    }
}
