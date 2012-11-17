package org.kercheval.gradle.buildrelease;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.kercheval.gradle.info.GradleInfoSource;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSTaskUtil;

public class BuildReleaseMergeTask
	extends DefaultTask
{
	@TaskAction
	public void doTask()
	{
		final Project project = getProject();
		final VCSTaskUtil vcsUtil = new VCSTaskUtil(project);

		try
		{
			//
			// Get the current release init task to obtain the branch and origin
			// variables
			//
			final BuildReleaseInitTask initTask = (BuildReleaseInitTask) new GradleInfoSource(project)
				.getTask(BuildReleasePlugin.INIT_TASK_NAME);

			//
			// Verify we are on the right branch to perform this task.
			//
			vcsUtil.validateWorkspaceBranchName(initTask.getReleasebranch());

			//
			// Verify the current workspace is clean
			//
			vcsUtil.validateWorkspaceIsClean();

			//
			// Validate the release branch is current. This is done by a
			// pull against the branch origin.
			//
			if (!initTask.isIgnoreorigin())
			{
				vcsUtil.getVCS().fetch(initTask.getRemoteorigin());

				//
				// Merge the remote origin to the release branch
				//
				vcsUtil.getVCS().merge(initTask.getReleasebranch(),
					initTask.getRemoteorigin());
				vcsUtil.getVCS().merge(initTask.getMainlinebranch(),
					initTask.getRemoteorigin());

				//
				// Push the new merge changes back to origin
				//
				vcsUtil.getVCS().push(initTask.getReleasebranch(),
					initTask.getRemoteorigin(), false);
			}
			else
			{
				//
				// Merge directly from the mainline branch
				//
				vcsUtil.getVCS().merge(initTask.getMainlinebranch(), null);
			}
		}
		catch (final VCSException e)
		{
			throw new TaskExecutionException(this, e);
		}
	}
}
