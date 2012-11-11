package org.kercheval.gradle.buildrelease;

import groovy.lang.Closure;

import java.io.File;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.execution.TaskExecutionGraphListener;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.kercheval.gradle.buildversion.BuildVersionPlugin;
import org.kercheval.gradle.buildversion.BuildVersionTagTask;
import org.kercheval.gradle.util.GradleUtil;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSTaskUtil;

public class BuildReleaseUploadTask
	extends DefaultTask
{
	private static final String DEFAULT_UPLOAD_TASK = "uploadArchives";
	private static final boolean DEFAULT_ONLYIFCLEAN = true;

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

	public BuildReleaseUploadTask()
	{
		final Project project = getProject();
		final Task thisTask = this;

		dependsOn(":" + getUploadtask());

		//
		// The magic happens in a doFirst installed at task graph completion.
		// We are assuming the build is a dependency on the upload task and
		// adding our tag and branch push to occur just prior to publication.
		//
		project.getGradle().getTaskGraph()
			.addTaskExecutionGraphListener(new TaskExecutionGraphListener()
			{
				@Override
				public void graphPopulated(final TaskExecutionGraph graph)
				{
					final AbstractTask uploadTask = (AbstractTask) new GradleUtil(project)
						.getTask(getUploadtask());

					if (null == uploadTask)
					{
						throw new TaskExecutionException(thisTask, new IllegalStateException(
							"The upload task '" + getUploadtask()
								+ "' specified for buildreleaseupdate does not exist"));
					}

					uploadTask.doFirst(new Closure<Task>(this, this)
					{
						@SuppressWarnings("unused")
						public Object doCall(final Task task)
						{
							tagAndPush(project, thisTask, graph.hasTask(thisTask));
							return task;
						}
					});
				}
			});

	}

	@TaskAction
	public void doTask()
	{
		//
		// This task has no direct action, it is all done through the doFirst()
		// closure on the upload task.
		//
	}

	public String getUploadtask()
	{
		return uploadtask;
	}

	public boolean isOnlyifclean()
	{
		return onlyifclean;
	}

	public void setOnlyifclean(final boolean onlyifclean)
	{
		this.onlyifclean = onlyifclean;
	}

	public void setUploadtask(final String uploadtask)
	{
		this.uploadtask = uploadtask;
	}

	private void tagAndPush(final Project project, final Task thisTask, final boolean forceOnBranch)
	{
		try
		{
			final Map<String, ?> props = project.getProperties();
			final VCSTaskUtil vcsUtil = new VCSTaskUtil((File) props.get("rootDir"), getProject()
				.getLogger());

			//
			// Get the current release init task to obtain the branch and origin
			// variables
			//
			final BuildReleaseInitTask initTask = (BuildReleaseInitTask) new GradleUtil(
				getProject()).getTask(BuildReleasePlugin.INIT_TASK_NAME);

			boolean isOnBranch = false;
			if (forceOnBranch)
			{
				//
				// Verify we are on the right branch to perform this task.
				//
				vcsUtil.validateWorkspaceBranchName(thisTask, initTask.getReleasebranch());
				isOnBranch = true;
			}
			else
			{
				isOnBranch = vcsUtil.getVCS().getBranchName().equals(initTask.getReleasebranch());
			}

			if (isOnBranch)
			{
				//
				// Verify the current workspace is clean
				//
				if (isOnlyifclean())
				{
					vcsUtil.validateWorkspaceIsClean(thisTask);
				}

				//
				// Get the tag task to tag the repository
				//
				final BuildVersionTagTask tagTask = (BuildVersionTagTask) new GradleUtil(
					getProject()).getTask(BuildVersionPlugin.TAG_TASK_NAME);
				tagTask.execute();

				//
				// Push the new created tags back to origin
				//
				if (!initTask.isIgnoreorigin())
				{
					vcsUtil.getVCS().pushBranch(initTask.getReleasebranch(),
						initTask.getRemoteorigin(), true);
				}
			}
		}
		catch (final VCSException e)
		{
			throw new TaskExecutionException(thisTask, e);
		}
	}

}
