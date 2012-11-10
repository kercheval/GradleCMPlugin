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

public class BuildReleasePushTask
	extends DefaultTask
{
	public BuildReleasePushTask()
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
			// Get the current release init task to obtain the branch and origin
			// variables
			//
			final BuildReleaseInitTask initTask = (BuildReleaseInitTask) new GradleUtil(
				getProject()).getTask(BuildReleasePlugin.INIT_TASK_NAME);

			//
			// Validate the release branch is current. This is done by a
			// pull against the branch origin.
			//
			vcs.fetchBranch(initTask.getReleasebranch(), initTask.getRemoteorigin());

			//
			// Merge the current branch to the release branch
			//

		}
		catch (final VCSException e)
		{
			throw new TaskExecutionException(this, e);
		}
	}
}
