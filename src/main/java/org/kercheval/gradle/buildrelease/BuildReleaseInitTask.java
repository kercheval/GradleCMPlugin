package org.kercheval.gradle.buildrelease;

import java.io.File;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.kercheval.gradle.buildvcs.BuildVCSPlugin;
import org.kercheval.gradle.buildvcs.BuildVCSTask;
import org.kercheval.gradle.util.GradleUtil;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSTaskUtil;

public class BuildReleaseInitTask
	extends DefaultTask
{
	public static final String DEFAULT_RELEASE_BRANCH = "release";
	public static final String DEFAULT_MAINLINE_BRANCH = "master";
	public static final String DEFAULT_REMOTE_ORIGIN = "origin";
	public static final boolean DEFAULT_IGNORE_ORIGIN = false;

	private static final String DEFAULT_UPLOAD_TASK = "uploadArchives";
	private static final boolean DEFAULT_ONLYIFCLEAN = true;

	//
	// The releasebranch variable defines the target branch for
	// release code promotion. This is the merge point and tag
	// target line for the project.
	//
	private String releasebranch = DEFAULT_RELEASE_BRANCH;

	//
	// The mainlinebranch variable defines the development branch
	// which is the principal source of code promotion into the release
	// branch. This is the source of merges.
	//
	private String mainlinebranch = DEFAULT_MAINLINE_BRANCH;

	//
	// the remoteorigin variable defaults to origin. This is a
	// lexicon inspired by git, but is reasonable and usable by
	// most VCS systems. VCS implementations can use this default
	// sentinal to imply they should use the default (or they may
	// ignore it altogether. In the git world, this means use the
	// remote ref/remote/origin as the release branch remote.
	//
	private String remoteorigin = DEFAULT_REMOTE_ORIGIN;

	//
	// If the ignoreorigin variable is set to true, then no
	// attempts will be made to push to or pull from origin
	// source repositories. This implies that all necessary
	// source code merges to and from remotes are done
	// manually and will restrict all repository changes to the
	// local repository.
	//
	private boolean ignoreorigin = DEFAULT_IGNORE_ORIGIN;

	//
	// The upload task is the normal publish task for the build artifacts. This
	// task will be hooked at task graph completion so that tagging and project
	// validation will occur prior to publication.
	//
	private String uploadtask = DEFAULT_UPLOAD_TASK;

	//
	// if onlyifclean is true, then the release will only occur
	// if the workspace is clean (no files checked out or modified).
	//
	private boolean onlyifclean = DEFAULT_ONLYIFCLEAN;

	@TaskAction
	public void doTask()
	{
		final Project project = getProject();
		final Map<String, ?> props = project.getProperties();
		final BuildVCSTask vcsTask = (BuildVCSTask) new GradleUtil(project)
			.getTask(BuildVCSPlugin.VCS_TASK_NAME);
		final VCSTaskUtil vcsUtil = new VCSTaskUtil(vcsTask.getType(), (File) props.get("rootDir"),
			project.getLogger());

		try
		{
			//
			// Verify the current workspace is clean
			//
			if (isOnlyifclean())
			{
				vcsUtil.validateWorkspaceIsClean(this);
			}

			vcsUtil.getVCS().createBranch(getMainlinebranch(), getRemoteorigin(), isIgnoreorigin());
			vcsUtil.getVCS().createBranch(getReleasebranch(), getRemoteorigin(), isIgnoreorigin());
		}
		catch (final VCSException e)
		{
			throw new TaskExecutionException(this, e);
		}
	}

	public String getMainlinebranch()
	{
		return mainlinebranch;
	}

	public String getReleasebranch()
	{
		return releasebranch;
	}

	public String getRemoteorigin()
	{
		return remoteorigin;
	}

	public String getUploadtask()
	{
		return uploadtask;
	}

	public boolean isIgnoreorigin()
	{
		return ignoreorigin;
	}

	public boolean isOnlyifclean()
	{
		return onlyifclean;
	}

	public void setIgnoreorigin(final boolean ignoreorigin)
	{
		this.ignoreorigin = ignoreorigin;
	}

	public void setMainlinebranch(final String mainlinebranch)
	{
		this.mainlinebranch = mainlinebranch;
	}

	public void setOnlyifclean(final boolean onlyifclean)
	{
		this.onlyifclean = onlyifclean;
	}

	public void setReleasebranch(final String releasebranch)
	{
		this.releasebranch = releasebranch;
	}

	public void setRemoteorigin(final String remoteorigin)
	{
		this.remoteorigin = remoteorigin;
	}

	public void setUploadtask(final String uploadtask)
	{
		this.uploadtask = uploadtask;
	}

}
