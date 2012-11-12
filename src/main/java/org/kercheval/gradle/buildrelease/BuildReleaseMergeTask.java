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

public class BuildReleaseMergeTask
	extends DefaultTask
{
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
			// Get the current release init task to obtain the branch and origin
			// variables
			//
			final BuildReleaseInitTask initTask = (BuildReleaseInitTask) new GradleUtil(project)
				.getTask(BuildReleasePlugin.INIT_TASK_NAME);

			//
			// Verify we are on the right branch to perform this task.
			//
			vcsUtil.validateWorkspaceBranchName(this, initTask.getReleasebranch());

			//
			// Verify the current workspace is clean
			//
			vcsUtil.validateWorkspaceIsClean(this);

			//
			// Validate the release branch is current. This is done by a
			// pull against the branch origin.
			//
			if (!initTask.isIgnoreorigin())
			{
				vcsUtil.getVCS().fetch(initTask.getRemoteorigin());
			}

			//
			// Merge the current branch to the release branch
			//
			vcsUtil.getVCS().mergeBranch(initTask.getRemoteorigin());

			//
			// Push the new merge changes back to origin
			//
			if (!initTask.isIgnoreorigin())
			{
				vcsUtil.getVCS().pushBranch(initTask.getReleasebranch(),
					initTask.getRemoteorigin(), false);
			}
		}
		catch (final VCSException e)
		{
			throw new TaskExecutionException(this, e);
		}
	}
}
