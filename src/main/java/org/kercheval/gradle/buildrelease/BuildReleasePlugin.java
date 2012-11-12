package org.kercheval.gradle.buildrelease;

import groovy.lang.Closure;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.execution.TaskExecutionGraphListener;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskExecutionException;
import org.kercheval.gradle.buildvcs.BuildVCSPlugin;
import org.kercheval.gradle.buildvcs.BuildVCSTask;
import org.kercheval.gradle.buildversion.BuildVersionPlugin;
import org.kercheval.gradle.buildversion.BuildVersionTagTask;
import org.kercheval.gradle.gradlecm.GradleCMPlugin;
import org.kercheval.gradle.util.GradleUtil;
import org.kercheval.gradle.vcs.IVCSAccess;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSTaskUtil;

public class BuildReleasePlugin
	implements Plugin<Project>
{
	public static final String INIT_TASK_NAME = "buildreleaseinit";
	public static final String MERGE_TASK_NAME = "buildreleasemerge";

	@Override
	public void apply(final Project project)
	{
		//
		// This plugin uses buildversion tasks
		//
		project.apply(new LinkedHashMap<String, String>()
		{
			{
				put("plugin", GradleCMPlugin.BUILD_VERSION_PLUGIN);
			}
		});

		//
		// Create the actual tasks that will be executed
		//
		final BuildReleaseInitTask buildInitTask = project.getTasks().add(INIT_TASK_NAME,
			BuildReleaseInitTask.class);
		buildInitTask
			.setDescription("Create a release branch structure supporting release code promotion and publication");
		buildInitTask.setGroup(GradleUtil.PLUGIN_GROUP_NAME);
		final BuildReleaseMergeTask buildMergeTask = project.getTasks().add(MERGE_TASK_NAME,
			BuildReleaseMergeTask.class);
		buildMergeTask
			.setDescription("Update the release branch with changes made to the mainline branch");
		buildMergeTask.setGroup(GradleUtil.PLUGIN_GROUP_NAME);

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
						.getTask(buildInitTask.getUploadtask());

					if ((null == uploadTask))
					{
						project
							.getLogger()
							.debug(
								"The upload task '"
									+ buildInitTask.getUploadtask()
									+ "' specified for buildreleaseupdate does not exist.  This task may be dynamic.");
					}
					else
					{
						uploadTask.doFirst(new Closure<Task>(this, this)
						{
							@SuppressWarnings("unused")
							public Object doCall(final Task task)
							{
								tagAndPush(project, buildInitTask, false);
								return task;
							}
						});
					}
				}
			});

	}

	private void tagAndPush(final Project project, final BuildReleaseInitTask currentTask,
		final boolean forceOnBranch)
	{
		try
		{
			final Map<String, ?> props = project.getProperties();
			final BuildVCSTask vcsTask = (BuildVCSTask) new GradleUtil(project)
				.getTask(BuildVCSPlugin.VCS_TASK_NAME);

			//
			// We cannot tag and push when we have no VCS. Silently fail...
			//
			if (!IVCSAccess.Type.NONE.toString().toLowerCase()
				.equals(vcsTask.getType().toLowerCase()))
			{
				final VCSTaskUtil vcsUtil = new VCSTaskUtil(vcsTask.getType(),
					(File) props.get("rootDir"), project.getLogger());

				//
				// Get the current release init task to obtain the branch and origin
				// variables
				//
				final BuildReleaseInitTask initTask = (BuildReleaseInitTask) new GradleUtil(project)
					.getTask(BuildReleasePlugin.INIT_TASK_NAME);

				boolean isOnBranch = false;
				if (forceOnBranch)
				{
					//
					// Verify we are on the right branch to perform this task.
					//
					vcsUtil.validateWorkspaceBranchName(currentTask, initTask.getReleasebranch());
					isOnBranch = true;
				}
				else
				{
					isOnBranch = vcsTask.getBranchName().equals(initTask.getReleasebranch());
				}

				if (isOnBranch)
				{
					//
					// Verify the current workspace is clean
					//
					if (currentTask.isOnlyifclean())
					{
						vcsUtil.validateWorkspaceIsClean(currentTask);
					}

					//
					// Get the tag task to tag the repository
					//
					final BuildVersionTagTask tagTask = (BuildVersionTagTask) new GradleUtil(
						project).getTask(BuildVersionPlugin.TAG_TASK_NAME);
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
				else
				{
					project.getLogger().info(
						"Workspace is not on branch '" + initTask.getReleasebranch()
							+ "'.  Build release tagging deactivated this execution of "
							+ currentTask.getUploadtask());
				}
			}
		}
		catch (final VCSException e)
		{
			throw new TaskExecutionException(currentTask, e);
		}
	}

}
