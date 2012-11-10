package org.kercheval.gradle.buildrelease;

import java.io.File;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.kercheval.gradle.util.GradleUtil;
import org.kercheval.gradle.vcs.IVCSAccess;
import org.kercheval.gradle.vcs.VCSAccessFactory;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSStatus;

public class BuildReleaseMergeTask
	extends DefaultTask
{
	public BuildReleaseMergeTask()
	{
		dependsOn(":" + BuildReleasePlugin.INIT_TASK_NAME);
	}

	@TaskAction
	public void doTask()
	{
		final Project project = getProject();
		final Map<String, ?> props = project.getProperties();
		final IVCSAccess vcs = VCSAccessFactory.getCurrentVCS((File) props.get("rootDir"),
			project.getLogger());

		try
		{
			//
			// Get the current release init task to obtain the branch and origin
			// variables
			//
			final BuildReleaseInitTask initTask = (BuildReleaseInitTask) new GradleUtil(
				getProject()).getTask(BuildReleasePlugin.INIT_TASK_NAME);

			//
			// Verify we are on the right branch to perform this task.
			//
			final String branchName = vcs.getBranchName();
			if (!branchName.equals(initTask.getReleasebranch()))
			{
				throw new TaskExecutionException(this, new IllegalStateException(
					"The current workspace is using the incorrect source branch.  Please checkout the '"
						+ initTask.getReleasebranch() + "' branch to continue."));
			}

			//
			// Verify the current workspace is clean
			//
			final VCSStatus status = vcs.getStatus();
			if (!status.isClean())
			{
				throw new TaskExecutionException(
					this,
					new IllegalStateException(
						"The current workspace is not clean.  Please ensure you have committed all outstanding work."));
			}

			//
			// Validate the release branch is current. This is done by a
			// pull against the branch origin.
			//
			if (!initTask.isIgnoreorigin())
			{
				vcs.fetchBranch(initTask.getReleasebranch(), initTask.getRemoteorigin());
			}

			//
			// Merge the current branch to the release branch
			//
			vcs.mergeBranch(initTask.getMainlinebranch());

			//
			// Push the new merge changes back to origin
			//
			if (!initTask.isIgnoreorigin())
			{
				vcs.pushBranch(initTask.getReleasebranch(), initTask.getRemoteorigin());
			}
		}
		catch (final VCSException e)
		{
			throw new TaskExecutionException(this, e);
		}
	}
}
