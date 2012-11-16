package org.kercheval.gradle.buildversion;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.kercheval.gradle.buildvcs.BuildVCSPlugin;
import org.kercheval.gradle.buildvcs.BuildVCSTask;
import org.kercheval.gradle.info.GradleInfoSource;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSTaskUtil;

public class BuildVersionTagTask
	extends DefaultTask
{
	public static final String DEFAULT_COMMENT = "Tag created by task "
		+ BuildVersionPlugin.TAG_TASK_NAME;
	public static final boolean DEFAULT_ONLYIFCLEAN = true;
	//
	// The comment is set to a string that will be placed in the comment
	// of the tag written to VCS
	//
	private String comment = DEFAULT_COMMENT;

	//
	// if onlyifclean is true, then tags are only written to the VCS system
	// if the workspace is clean (no files checked out or modified). This
	// will prevent tagging based on old commits or build releases that are not
	// replicable.
	//
	private boolean onlyifclean = DEFAULT_ONLYIFCLEAN;

	public BuildVersionTagTask()
	{
		dependsOn(":" + BuildVersionPlugin.VERSION_TASK_NAME);
	}

	@TaskAction
	public void doTask()
	{
		final Project project = getProject();
		if (project.getVersion() instanceof BuildVersion)
		{
			final BuildVCSTask vcsTask = (BuildVCSTask) new GradleInfoSource(project)
				.getTask(BuildVCSPlugin.VCS_TASK_NAME);
			final VCSTaskUtil vcsUtil = new VCSTaskUtil(project);

			if (isOnlyifclean())
			{
				//
				// Tags should be written only if the workspace is clean.
				//
				vcsUtil.validateWorkspaceIsClean();
			}

			//
			// Write a tag into VCS using the current project version
			//
			try
			{
				vcsTask.createTag(project.getVersion().toString(), getComment());
				project.getLogger().info(
					"Tag '" + project.getVersion() + "' written to VCS with comment '"
						+ getComment() + "'");
			}
			catch (final VCSException e)
			{
				throw new TaskExecutionException(this, e);
			}
		}
		else
		{
			throw new TaskExecutionException(
				this,
				new IllegalStateException(
					"Project version is not of type BuildVersion: ensure buildversion type task has been run or set project version to an object of type BuildVersion."));
		}
	}

	public String getComment()
	{
		return comment;
	}

	public boolean isOnlyifclean()
	{
		return onlyifclean;
	}

	public void setComment(final String comment)
	{
		this.comment = comment;
	}

	public void setOnlyifclean(final boolean onlyifclean)
	{
		this.onlyifclean = onlyifclean;
	}
}
