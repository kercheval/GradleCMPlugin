package org.kercheval.gradle.buildversion;

import java.io.File;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.execution.TaskExecutionGraphListener;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.kercheval.gradle.vcs.VCSException;
import org.kercheval.gradle.vcs.VCSTag;
import org.kercheval.gradle.vcs.VCSTaskUtil;

public class BuildVersionTask
	extends DefaultTask
{
	public static final boolean DEFAULT_AUTOWRITE = true;
	public static final boolean DEFAULT_AUTOINCREMENT = true;
	public static final boolean DEFAULT_USETAG = true;

	//
	// When autowrite is true, the project version will automatically be set at
	// task graph completion. This is normally the correct behavior, but turning
	// this variable to false allows late binding in some cases.
	//
	private boolean autowrite = DEFAULT_AUTOWRITE;

	//
	// If increment is true, then during task execution, the last tag found will
	// be incremented. For example if the tag was 4.5, the version placed in the
	// project would be 4.6. This is normally the correct behavior. Turning this
	// variable off will result in the version staying at exactly the found or set
	// value.
	//
	private boolean autoincrement = DEFAULT_AUTOINCREMENT;

	//
	// If usetag is true, then when the task is run, all tags that match the validate
	// pattern will be iterated and the most recent tag will be used to determine the
	// version values. If set to false, then the version variables must be set in
	// the configuration section of the gradle.build file.
	//
	private boolean usetag = DEFAULT_USETAG;

	//
	// This is the object that will be set at the project version. This is normally
	// updated via a tag search during task execution, but doLast handlers can modify this
	// at will. Updates to this object will be reflected in project.version (which could
	// also be directly modified after a cast).
	//
	private BuildVersion version = new BuildVersion(null, 0, 0, 0, null);

	public BuildVersionTask()
	{

		//
		// Add a listener to obtain the current version information from the system.
		// This is done here to ensure all configuration parameters have been written.
		//
		final Project project = getProject();

		project.getGradle().getTaskGraph()
			.addTaskExecutionGraphListener(new TaskExecutionGraphListener()
			{
				@Override
				public void graphPopulated(final TaskExecutionGraph graph)
				{

					//
					// Set the version automagically unless we have explicitly been
					// told not to set the version
					//
					if (isAutowrite())
					{
						execute();
					}
				}
			});
	}

	@TaskAction
	public void doTask()
	{
		final Project project = getProject();

		//
		// Get the version from VCS and set the project version to our shiny new
		// version object. The project version is used by most other tasks during
		// execution for naming purposes.
		//
		setVersion(getVersionFromVCS(project));

		if (isAutoincrement())
		{
			getVersion().incrementVersion();
		}

		project.setVersion(getVersion());
	}

	public BuildVersion getVersion()
	{
		return version;
	}

	private BuildVersion getVersionFromVCS(final Project project)
	{
		BuildVersion rVal = getVersion();

		if (isUsetag())
		{
			final Map<String, ?> props = project.getProperties();

			//
			// Get the filtered list of tags from VCS and iterate to find the newest one.
			//
			final VCSTaskUtil vcsUtil = new VCSTaskUtil((File) props.get("rootDir"), getProject()
				.getLogger());
			List<VCSTag> tagList;

			try
			{
				tagList = vcsUtil.getVCS().getTags(getVersion().getValidatePattern());
			}
			catch (final VCSException e)
			{
				throw new TaskExecutionException(this, e);
			}

			VCSTag foundTag = null;

			for (final VCSTag tag : tagList)
			{
				if (null == foundTag)
				{
					foundTag = tag;
				}
				else
				{
					if (foundTag.getCommitDate().before(tag.getCommitDate()))
					{
						foundTag = tag;
					}
				}
			}

			//
			// If we found a matching tag, generate the build version based on that tag name
			// and return
			//
			if (null != foundTag)
			{
				try
				{
					rVal = new BuildVersion(rVal.getPattern(), rVal.getValidatePattern(),
						foundTag.getName());
				}
				catch (final ParseException e)
				{
					project.getLogger()
						.error(
							"Unable to generate version from tag '" + foundTag + "': "
								+ e.getMessage());

					throw new TaskExecutionException(this, e);
				}
			}
		}

		return rVal;
	}

	public boolean isAutoincrement()
	{
		return autoincrement;
	}

	protected boolean isAutowrite()
	{
		return autowrite;
	}

	public boolean isUsetag()
	{
		return usetag;
	}

	public void setAutoincrement(final boolean autoincrement)
	{
		this.autoincrement = autoincrement;
	}

	public void setAutowrite(final boolean autowrite)
	{
		this.autowrite = autowrite;
	}

	public void setUsetag(final boolean usetag)
	{
		this.usetag = usetag;
	}

	public void setVersion(final BuildVersion version)
	{
		this.version = version;
	}
}
