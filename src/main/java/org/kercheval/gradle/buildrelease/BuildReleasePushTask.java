package org.kercheval.gradle.buildrelease;

import java.io.File;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.kercheval.gradle.vcs.IVCSAccess;
import org.kercheval.gradle.vcs.VCSAccessFactory;

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

// try
// {
		System.out.println("Running build release push task: " + vcs.toString());
// }
// catch (final VCSException e)
// {
// throw new TaskExecutionException(this, e);
// }
	}
}
