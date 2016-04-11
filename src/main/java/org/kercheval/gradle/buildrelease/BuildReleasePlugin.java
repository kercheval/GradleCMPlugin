package org.kercheval.gradle.buildrelease;

import java.util.LinkedHashMap;

import org.gradle.api.*;
import org.gradle.api.execution.*;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.*;
import org.kercheval.gradle.buildvcs.*;
import org.kercheval.gradle.gradlecm.GradleCMPlugin;
import org.kercheval.gradle.info.GradleInfoSource;
import org.kercheval.gradle.vcs.*;

public class BuildReleasePlugin implements Plugin<Project> {
    public static final String INIT_TASK_NAME = "buildreleaseinit";

    public static final String MERGE_TASK_NAME = "buildreleasemerge";

    public static final String RELEASE_TASK_NAME = "buildrelease";

    @Override
    public void apply(final Project project) {
        //
        // This plugin uses buildversion tasks
        //
        project.apply(new LinkedHashMap<String, Class>() {
            {
                put("plugin", GradleCMPlugin.BUILD_VERSION_PLUGIN);
            }
        });

        //
        // Create the actual tasks that will be executed
        //
        final BuildReleaseInitTask buildInitTask =
                project.getTasks().create(INIT_TASK_NAME, BuildReleaseInitTask.class);
        buildInitTask.setDescription(
            "Create a release branch structure supporting release code promotion and publication");
        buildInitTask.setGroup(GradleInfoSource.PLUGIN_GROUP_NAME);
        final BuildReleaseMergeTask buildMergeTask =
                project.getTasks().create(MERGE_TASK_NAME, BuildReleaseMergeTask.class);
        buildMergeTask
            .setDescription("Update the release branch with changes made to the mainline branch");
        buildMergeTask.setGroup(GradleInfoSource.PLUGIN_GROUP_NAME);
        final BuildReleaseTask buildReleaseTask =
                project.getTasks().create(RELEASE_TASK_NAME, BuildReleaseTask.class);
        buildReleaseTask.setDescription("Run the " + MERGE_TASK_NAME + " and upload targets");
        buildReleaseTask.setGroup(GradleInfoSource.PLUGIN_GROUP_NAME);

        //
        // The magic happens in a doFirst installed at task graph completion.
        // We are assuming the build is a dependency on the upload task and
        // adding our tag and branch push to occur just prior to publication.
        //
        project.getGradle().getTaskGraph()
            .addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
                @Override
                public void graphPopulated(final TaskExecutionGraph graph) {
                    final AbstractTask uploadTask = (AbstractTask) new GradleInfoSource(project)
                        .getTask(buildInitTask.getUploadtask());

                    if ((null == uploadTask)) {
                        project.getLogger().debug("The upload task '"
                                + buildInitTask.getUploadtask()
                                + "' specified for buildreleaseupdate does not exist.  This task may be dynamic.");
                    } else {
                        project.getGradle().addListener(new TaskExecutionListener() {
                            @Override
                            public void afterExecute(final Task task, final TaskState taskState) {
                                // Nothing after execution
                            }

                            @Override
                            public void beforeExecute(final Task task) {
                                //
                                // If we have the right task, ensure we run the tag and push
                                //
                                if (task.equals(uploadTask)) {
                                    tagAndPush(project, buildInitTask, false);
                                }
                            }
                        });
                    }
                }
            });
    }

    protected void tagAndPush(final Project project, final BuildReleaseInitTask currentTask,
            final boolean forceOnBranch) {
        try {
            final BuildVCSTask vcsTask = (BuildVCSTask) new GradleInfoSource(project)
                .getTask(BuildVCSPlugin.VCS_TASK_NAME);

            //
            // We cannot tag and push when we have no VCS. Silently fail...
            //
            if (!VCSAccess.Type.NONE.toString().toLowerCase()
                .equals(vcsTask.getType().toLowerCase())) {
                final VCSTaskUtil vcsUtil = new VCSTaskUtil(project);

                //
                // Get the current release init task to obtain the branch and origin
                // variables
                //
                final BuildReleaseInitTask initTask =
                        (BuildReleaseInitTask) new GradleInfoSource(project)
                            .getTask(BuildReleasePlugin.INIT_TASK_NAME);

                if (forceOnBranch) {
                    //
                    // Verify we are on the right branch to perform this task.
                    //
                    vcsUtil.validateWorkspaceBranchName(initTask.getReleasebranch());
                }

                if (vcsTask.getBranchName().equals(initTask.getReleasebranch())) {
                    //
                    // Verify the current workspace is clean
                    //
                    if (currentTask.isOnlyifclean()) {
                        vcsUtil.validateWorkspaceIsClean();
                    }

                    //
                    // Get the tag task to tag the repository
                    //
                    final String tagName = project.getVersion().toString();
                    vcsTask.createTag(tagName, "Tag created by task " + initTask.getUploadtask());

                    //
                    // Push the new created tag back to origin
                    //
                    if (!initTask.isIgnoreorigin()) {
                        vcsUtil.getVCS().push(tagName, initTask.getRemoteorigin(), true);
                    }
                } else {
                    project.getLogger()
                        .info("Workspace is not on branch '" + initTask.getReleasebranch()
                                + "'.  Build release tagging deactivated this execution of "
                                + currentTask.getUploadtask());
                }
            }
        } catch (final VCSException e) {
            throw new TaskExecutionException(currentTask, e);
        }
    }

}
