package org.kercheval.gradle.buildinfo;

import groovy.lang.Closure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.execution.TaskExecutionGraphListener;
import org.gradle.api.file.CopySpec;
import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.kercheval.gradle.buildvcs.BuildVCSPlugin;
import org.kercheval.gradle.buildvcs.BuildVCSTask;
import org.kercheval.gradle.util.GradleUtil;
import org.kercheval.gradle.util.JenkinsUtil;
import org.kercheval.gradle.util.MachineUtil;
import org.kercheval.gradle.util.SortedProperties;
import org.kercheval.gradle.vcs.IVCSAccess;
import org.kercheval.gradle.vcs.VCSAccessFactory;
import org.kercheval.gradle.vcs.VCSException;

public class BuildInfoTask
	extends DefaultTask
{
	public static final String DEFAULT_FILENAME = "buildinfo.properties";
	public static final boolean DEFAULT_AUTOWRITE = true;
	private static final Map<String, String> DEFAULT_TASKMAP_PRIVATE = new HashMap<String, String>();
	static
	{
		DEFAULT_TASKMAP_PRIVATE.put("jar", "META-INF");
		DEFAULT_TASKMAP_PRIVATE.put("war", "META-INF");
		DEFAULT_TASKMAP_PRIVATE.put("ear", "META-INF");
	}
	public static final Map<String, String> DEFAULT_TASKMAP = Collections
		.unmodifiableMap(DEFAULT_TASKMAP_PRIVATE);

	static private final String EOL = System.getProperty("line.separator");

	//
	// If true, the buildinfo file will be written automatically at
	// the beginning of the task evaluation phase for the project.
	//
	private boolean autowrite = DEFAULT_AUTOWRITE;

	//
	// This is the file name to use for info file. The default value
	// for this filename is buildinfo.properties
	//
	private String filename = DEFAULT_FILENAME;

	//
	// This directory represents the path used for the file written.
	// This value will default to ${buildDir}
	//
	private String filedir;

	//
	// This is the map of tasks/locations that will copy elements added so
	// that the build info file will be added into the output.
	//
	private Map<String, String> taskmap = DEFAULT_TASKMAP;

	//
	// This map represents custom information should be placed in the file written
	//
	private Map<String, Object> custominfo = new HashMap<String, Object>();

	//
	// This is the original buildDir to determine if it has changed during
	// task graph build. The filedirWasSet variable is used to determine if the
	//
	// TODO: Need to test. If filedirWasSet is reliable, then just always update from buildDir
	private String originalBuildDir;
	private boolean filedirWasSet = false;

	public BuildInfoTask()
	{
		final Project project = getProject();
		final Task thisTask = this;

		//
		// Init the file directory
		//
		final Map<String, ?> props = project.getProperties();
		try
		{
			originalBuildDir = ((File) props.get("buildDir")).getCanonicalPath();
			setFiledir(originalBuildDir);
		}
		catch (final IOException e)
		{
			project.getLogger().error(e.getMessage());

			throw new TaskExecutionException(this, e);
		}

		//
		// Add a listener to automatically write the build info file as soon
		// as the task graph is completed. This ensures that all tasks have
		// completed their configuration phase and all variable updates have
		// been completed for the purposes of this plugin.
		//
		project.getGradle().getTaskGraph()
			.addTaskExecutionGraphListener(new TaskExecutionGraphListener()
			{
				@Override
				public void graphPopulated(final TaskExecutionGraph graph)
				{

					final Map<String, ?> afterGraphProps = project.getProperties();
					try
					{
						//
						// If the buildDir was reset at config and it is the same as was initially
						// created, then reset to the new buildDir.
						//
						final String newBuildDir = ((File) afterGraphProps.get("buildDir"))
							.getCanonicalPath();
						if (!filedirWasSet && getFiledir().equals(originalBuildDir))
						{
							setFiledir(newBuildDir);
						}
					}
					catch (final IOException e)
					{

						project.getLogger().error(e.getMessage());

						throw new TaskExecutionException(thisTask, e);
					}

					//
					// Run our task and insert into tasks if autowrite
					//
					if (isAutowrite())
					{
						execute();

						final Map<String, Task> tasknameMap = new HashMap<String, Task>();

						for (final Task task : graph.getAllTasks())
						{
							tasknameMap.put(task.getName(), task);
						}

						for (final String taskname : taskmap.keySet())
						{
							final Task task = tasknameMap.get(taskname);

							if (null != task)
							{

								//
								// The task must implement AbstractCopyTask in order
								// to automatically insert.
								//
								if (task instanceof AbstractCopyTask)
								{

									//
									// Add a copy spec into the task using a closure
									//
									project.getLogger().info(
										"buildinfo: copy spec being added to task: "
											+ task.getPath());
									((AbstractCopyTask) task).from(getFiledir(),
										new Closure<CopySpec>(this, this)
										{

											//
											// Groovy closure creation in Java is a bit odd since
											// you need to know the magic. doCall must be
											// defined and the parameter being passed is done via
											// reflection. This allows pretty clean
											// interaction.
											//
											@SuppressWarnings("unused")
											public CopySpec doCall(final CopySpec copySpec)
											{

												//
												// This closure is being sent a child copy spec,
												// add in the from and include parameters for
												// the child spec
												//
												copySpec.into(taskmap.get(taskname)).include(
													getFilename());

												return copySpec;
											}
										});
								}
								else
								{

									//
									// Not supported task!
									//
									project.getLogger().error(
										"buildinfo: task defined in taskmap must implement AbstractCopyTask: "
											+ task.getPath());
								}
							}
							else
							{
								if (getTaskmap() != DEFAULT_TASKMAP)
								{

									//
									// Report tasks which don't exist (if not using default)
									//
									project.getLogger().info(
										"buildinfo: task defined in taskmap does not exist: "
											+ taskname);
								}
							}
						}
					}
				}
			});
	}

	@TaskAction
	public void doTask()
	{

		//
		// Obtain the project properties
		//
		final Project project = getProject();
		final Map<String, ?> props = project.getProperties();
		final BuildVCSTask vcsTask = (BuildVCSTask) new GradleUtil(project)
			.getTask(BuildVCSPlugin.VCS_TASK_NAME);

		try
		{

			//
			// Use mkdir support to ensure the directory exists
			//
			final File buildDirFile = new File(getFiledir());

			project.mkdir(buildDirFile);

			//
			// Create the file spec for our file writer
			//
			final StringBuilder sb = new StringBuilder();

			sb.append(buildDirFile.getCanonicalPath());
			sb.append("/");
			sb.append(getFilename());

			final BufferedWriter out = new BufferedWriter(new FileWriter(sb.toString()));

			//
			// Write out the header
			//
			out.write("#");
			out.write(EOL);
			out.write("# Build Info Created by ");
			out.write(getName());
			out.write(" on ");
			out.write(new Date().toString());
			out.write(EOL);
			out.write("#");
			out.write(EOL);

			try
			{
				final List<Task> taskList = project.getGradle().getTaskGraph().getAllTasks();

				out.write("# Tasks executing in this build");
				out.write(EOL);

				//
				// Include all the tasks that are to be executed in this build.
				// These are scheduled tasks, there is no guarantee that the tasks
				// will actually be run or will have succeeded if run
				//
				for (final Task task : taskList)
				{
					out.write("#   ");
					out.write(task.getPath());
					out.write(EOL);
				}

				out.write("#");
				out.write(EOL);
				out.write("#");
				out.write(EOL);
			}
			catch (final IllegalStateException e)
			{

				// Ignore if doTask called prior to task graph ready
			}

			out.write(EOL);

			//
			// If custom info is specified in the gradle build file, go ahead and place that
			// in the info file at the beginning
			//
			if ((getCustominfo() != null) && !getCustominfo().isEmpty())
			{
				final Properties customProps = new SortedProperties();

				for (final Entry<String, Object> entry : getCustominfo().entrySet())
				{
					customProps.put("custom.info." + entry.getKey().toString(), entry.getValue()
						.toString());
				}

				customProps.store(out, "Custom build info specified in gradle build file");
				out.write(EOL);
				out.write(EOL);
			}

			//
			// Grab properties from our various information sources
			//
			new MachineUtil(project).getMachineInfo().store(out, "Machine Info");
			out.write(EOL);
			out.write(EOL);
			new GradleUtil(project).getGradleInfo().store(out, "Gradle Info");
			out.write(EOL);
			out.write(EOL);
			final IVCSAccess vcs = VCSAccessFactory.getCurrentVCS(vcsTask.getType(),
				(File) props.get("rootDir"), project.getLogger());
			final Properties vcsprops = vcs.getInfo();
			vcsprops.setProperty("vcs.type", vcs.getType().toString());
			vcsprops.store(out, "VCS Info");
			out.write(EOL);
			out.write(EOL);
			new JenkinsUtil().getJenkinsInfo().store(out, "Jenkins Info");
			out.close();
		}
		catch (final IOException e)
		{
			throw new TaskExecutionException(this, e);
		}
		catch (final VCSException e)
		{
			throw new TaskExecutionException(this, e);
		}
	}

	public Map<String, Object> getCustominfo()
	{
		return custominfo;
	}

	public String getFiledir()
	{
		return filedir;
	}

	public String getFilename()
	{
		return filename;
	}

	public Map<String, String> getTaskmap()
	{
		return taskmap;
	}

	public boolean isAutowrite()
	{
		return autowrite;
	}

	public void setAutowrite(final boolean autowrite)
	{
		this.autowrite = autowrite;
	}

	public void setCustominfo(final Map<String, Object> custominfo)
	{
		this.custominfo = custominfo;
	}

	public void setFiledir(final String filedir)
	{
		filedirWasSet = true;
		this.filedir = filedir;
	}

	public void setFilename(final String filename)
	{
		this.filename = filename;
	}

	public void setTaskmap(final Map<String, String> taskmap)
	{
		this.taskmap = taskmap;
	}
}
