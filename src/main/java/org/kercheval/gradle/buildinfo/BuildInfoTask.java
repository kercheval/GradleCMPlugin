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
import org.kercheval.gradle.info.GradleInfoSource;
import org.kercheval.gradle.info.HudsonInfoSource;
import org.kercheval.gradle.info.InfoSource;
import org.kercheval.gradle.info.JenkinsInfoSource;
import org.kercheval.gradle.info.MachineInfoSource;
import org.kercheval.gradle.info.SortedProperties;
import org.kercheval.gradle.info.TeamCityInfoSource;

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
	public static final boolean DEFAULT_SHOWINFO_SECTION = true;

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
	// These boolean values represent information to be enabled in output
	//
	private boolean showtaskinfo = DEFAULT_SHOWINFO_SECTION;
	private boolean showgradleinfo = DEFAULT_SHOWINFO_SECTION;
	private boolean showmachineinfo = DEFAULT_SHOWINFO_SECTION;
	private boolean showvscinfo = DEFAULT_SHOWINFO_SECTION;
	private boolean showciinfo = DEFAULT_SHOWINFO_SECTION;

	// The filedirWasSet variable is used to determine if the filedir has
	// explicitly been set in the build file. If not, we update our internal
	// filedir variable to buildDir just in case it was changed last minute
	// during configuration.
	//
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
			setFiledir(((File) props.get("buildDir")).getCanonicalPath());
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
						if (!filedirWasSet)
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
		project.getProperties();
		final BuildVCSTask vcsTask = (BuildVCSTask) new GradleInfoSource(project)
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

			if (isShowtaskinfo())
			{
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
			if (isShowmachineinfo())
			{
				maybeStoreProperties(out, new MachineInfoSource(project));
			}
			if (isShowgradleinfo())
			{
				maybeStoreProperties(out, new GradleInfoSource(project));
			}
			if (isShowvscinfo())
			{
				maybeStoreProperties(out, vcsTask.getInfoSource());
			}
			if (isShowciinfo())
			{
				maybeStoreProperties(out, new JenkinsInfoSource());
				maybeStoreProperties(out, new HudsonInfoSource());
				maybeStoreProperties(out, new TeamCityInfoSource());
			}
			out.close();
		}
		catch (final IOException e)
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

	public boolean isShowciinfo()
	{
		return showciinfo;
	}

	public boolean isShowgradleinfo()
	{
		return showgradleinfo;
	}

	public boolean isShowmachineinfo()
	{
		return showmachineinfo;
	}

	public boolean isShowtaskinfo()
	{
		return showtaskinfo;
	}

	public boolean isShowvscinfo()
	{
		return showvscinfo;
	}

	private void maybeStoreProperties(final BufferedWriter out, final InfoSource infoSource)
		throws IOException
	{
		if (infoSource.isActive())
		{
			infoSource.getInfo().store(out, infoSource.getDescription());
			out.write(EOL);
			out.write(EOL);
		}
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

	public void setShowciinfo(final boolean showciinfo)
	{
		this.showciinfo = showciinfo;
	}

	public void setShowgradleinfo(final boolean showgradleinfo)
	{
		this.showgradleinfo = showgradleinfo;
	}

	public void setShowmachineinfo(final boolean showmachineinfo)
	{
		this.showmachineinfo = showmachineinfo;
	}

	public void setShowtaskinfo(final boolean showtaskinfo)
	{
		this.showtaskinfo = showtaskinfo;
	}

	public void setShowvscinfo(final boolean showvscinfo)
	{
		this.showvscinfo = showvscinfo;
	}

	public void setTaskmap(final Map<String, String> taskmap)
	{
		this.taskmap = taskmap;
	}
}
