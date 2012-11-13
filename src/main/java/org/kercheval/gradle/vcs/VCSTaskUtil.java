package org.kercheval.gradle.vcs;

import java.io.File;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskExecutionException;
import org.kercheval.gradle.buildvcs.BuildVCSPlugin;
import org.kercheval.gradle.buildvcs.BuildVCSTask;
import org.kercheval.gradle.util.GradleUtil;

public class VCSTaskUtil
{
	final BuildVCSTask vcsTask;
	final IVCSAccess vcs;

	public VCSTaskUtil(final Project project)
	{
		final Map<String, ?> props = project.getProperties();
		vcsTask = (BuildVCSTask) new GradleUtil(project).getTask(BuildVCSPlugin.VCS_TASK_NAME);
		vcs = VCSAccessFactory.getCurrentVCS(vcsTask.getType(), (File) props.get("rootDir"),
			project.getLogger());
	}

	public IVCSAccess getVCS()
	{
		return vcs;
	}

	public BuildVCSTask getVCSTask()
	{
		return vcsTask;
	}

	public void validateWorkspaceBranchName(final String validateBranchName)
	{
		try
		{
			final String branchName = getVCS().getBranchName();
			if (!branchName.equals(validateBranchName))
			{
				throw new TaskExecutionException(vcsTask, new IllegalStateException(
					"The current workspace is using the incorrect source branch.  Please checkout the '"
						+ validateBranchName + "' branch to continue."));
			}
		}
		catch (final VCSException e)
		{
			throw new TaskExecutionException(vcsTask, e);
		}
	}

	public void validateWorkspaceIsClean()
	{
		try
		{
			final VCSStatus status = getVCS().getStatus();
			if (!status.isClean())
			{
				throw new TaskExecutionException(
					vcsTask,
					new IllegalStateException(
						"The current workspace is not clean.  Please ensure you have committed all outstanding work."));
			}
		}
		catch (final VCSException e)
		{
			throw new TaskExecutionException(vcsTask, e);
		}
	}
}
