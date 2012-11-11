package org.kercheval.gradle.vcs;

import java.io.File;

import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskExecutionException;

public class VCSTaskUtil
{
	final IVCSAccess vcs;

	public VCSTaskUtil(final String type, final File rootDir, final Logger logger)
	{
		vcs = VCSAccessFactory.getCurrentVCS(type, rootDir, logger);
	}

	public IVCSAccess getVCS()
	{
		return vcs;
	}

	public void validateWorkspaceBranchName(final Task task, final String validateBranchName)
	{
		try
		{
			final String branchName = getVCS().getBranchName();
			if (!branchName.equals(validateBranchName))
			{
				throw new TaskExecutionException(task, new IllegalStateException(
					"The current workspace is using the incorrect source branch.  Please checkout the '"
						+ validateBranchName + "' branch to continue."));
			}
		}
		catch (final VCSException e)
		{
			throw new TaskExecutionException(task, e);
		}
	}

	public void validateWorkspaceIsClean(final Task task)
	{
		try
		{
			final VCSStatus status = getVCS().getStatus();
			if (!status.isClean())
			{
				throw new TaskExecutionException(
					task,
					new IllegalStateException(
						"The current workspace is not clean.  Please ensure you have committed all outstanding work."));
			}
		}
		catch (final VCSException e)
		{
			throw new TaskExecutionException(task, e);
		}
	}
}
