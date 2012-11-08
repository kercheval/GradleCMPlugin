package org.kercheval.gradle.buildversion;

import java.io.File;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.kercheval.gradle.vcs.IVCSAccess;
import org.kercheval.gradle.vcs.VCSAccessFactory;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSStatus;
import org.kercheval.gradle.vcs.VCSTag;

public class BuildVersionTagTask
	extends DefaultTask
{
	public static final String DEFAULT_COMMENT = "Tag created by "
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
		dependsOn(":" + BuildVersionPlugin.MAIN_TASK_NAME);
	}

	@TaskAction
	public void doTask()
	{
		if (getProject().getVersion() instanceof BuildVersion)
		{
			final Map<String, ?> props = getProject().getProperties();
			final IVCSAccess vcs = VCSAccessFactory.getCurrentVCS((File) props.get("rootDir"),
				getProject().getLogger());

			if (isOnlyifclean())
			{

				//
				// Tags should be written only if the workspace is clean.
				//
				VCSStatus status;

				try
				{
					status = vcs.getStatus();
				}
				catch (final VCSException e)
				{
					throw new TaskExecutionException(this, e);
				}

				if (!status.isClean())
				{
					throw new TaskExecutionException(
						this,
						new IllegalStateException(
							"The current workspace is not clean.  Please ensure you have committed all outstanding work."));
				}
			}

			//
			// Write a tag into VCS using the current project version
			//
			try
			{
				final VCSTag tag = new VCSTag(getProject().getVersion().toString(), comment);

				vcs.createTag(tag);
				getProject().getLogger().info(
					"Tag '" + tag.getName() + "' written to VCS with comment '" + tag.getComment()
						+ "'");
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
					"Project version is not of type BuildVersion: ensure buildversion task has been run or set project version to an object of type BuildVersion."));
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
