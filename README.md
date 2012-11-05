#Gradle Configuration Management Build Support

## Project Summary

This project is a set of plugins intended to support standard
configuration management practices that are not necessarily well
supported in gradle.  

[Build Info Plugin](#build-info-plugin)
- [Quick Start](#build-info-quick-start)
- [Variables](#variables)
- [Examples](#examples)

[Build Version Plugin](#build-version-plugin)
- [Quick Start](#quick-start-1)
- [Tasks](#tasks)
- [Variables](#variables-1)
- [Examples](#examples-1)

I intend for this source code base to be instructional as well as
useful.

The sources here demonstrate the following

- Creation of Gradle plugins and tasks using standard Java
- Automatic task run based on the gradle lifecycle.
- Use of the Gradle API for task customization and iteration
- Use of the Gradle API for hooking existing tasks
- Use of java based closure implementation to extend gradle tasks
- Use of JGit to obtain VCS status and to read and set tags

##Build Info Plugin

###Summary

The buildinfo plugin supports the creation of a file (in standard Java
properties format) that shows environment and build information
present at the time a build takes place.  The primary information
gathered includes:

- Obtains Git repository information about the build
- Obtains build machine information and user info
- Obtains Jenkins CI build system information
- Obtains Gradle property information

In addition to the information above, the buildinfo configuration
block can be used to add custom information to the build file.

###Quick Start<a id="build-info-quick-start" />

Add a buildscript section for the plugin dependency in your build
gradle file.  Note that the example below will take the most recent
released plugin jar file available.

```
buildscript {
	repositories {
		mavenCentral()
		mavenRepo url: 'http://kercheval.org/mvn-repo/releases'
	}
	dependencies {
		classpath 'org.kercheval:GradleCMPlugin:+'
	}
}
```

Add an apply line to your gradle build file.

```
apply plugin: 'buildinfo'
```

This will cause a file called buildinfo.properties to be placed within
your ${buildDir} directory and will automatically insert the
buildinfo.properties file into all generated jar, war and ear files.

###Variables

By default, the plugin will create the information file in the default
build directory, name the file buildinfo.properties and insert this
file into the META-INF directory of all created JAR/WAR/EAR files
created in the build.  All of these behaviors are modifiable by
setting custom variables in your gradle build file in the 'buildinfo'
task configuration as illustrated in the examples below.

<table style="border: 1px solid black;">
	<tr>
		<th>Variable</td>
		<th>Description</td>
	</tr>
	<tr>
		<td>filename</td>
		<td>
<p>
Default: <strong>buildinfo.properties</strong>
</p>
<p>
The filename variable determines the name of the file (without path)
of the properties file created.  The filename can be any valid name
and extension for your target systems.  
</p>
		</td>
	</tr>
	<tr>
		<td>filedir</td>
		<td>
<p>
Default: <strong>${buildDir}
</p>
<p>
The filedir variable determines the path to the build info properties
file that is generated.  The default is the build directory for
gradle, but this can be any path at all in the file system.
</p>
		</td>
	</tr>
	<tr>
		<td>custominfo</td>
		<td>
<p>
Default: <strong>no default info defined</strong>
</p>
<p>
The custominfo variable allows the specification of arbitrary
properties that will be placed in the build info file.  This map
specifies a name and an object (mapped via the normal string
representation).  All properties in this variable will be output to
the build properties file with a prefix of "custom.info.".  See
examples below for standard usage.
</p>
		</td>
	</tr>
	<tr>
		<td>taskmap</td>
		<td>
<p>
Default: <strong>[jar: "META-INF", war: "META-INF", ear: "META-INF"]</strong>
</p>
<p>
The taskmap variable holds a map of targets and the directory in the
copy target that the build info file will be copied into.  If the info
file should be at the root of the copy target, use an empty string as
the target directory.
</p>
<p>
Note: when creating a taskmap variable, you are overriding the
default values.  If you intend to simply add another task, you will
need to supply the default values for jar/war/ear targets in your
taskmap.  This behavior ensures you can override all the default
behaviors.  Note that if you just want the info file built
automatically but no task modification at all, you can set the
taskmap to an empty map and no tasks will be modified.
</p>
<p>
The tasks specified must be derived from the task type
AbstractCopyTask.  This includes almost all archive and copy style
tasks in gradle (such as copy, sync, tar, zip, jar, war, ear).  Tasks
that are not found will be ignored from this list, though an info
(--info) message will be logged that a task was not found.
</p>
<p>
Warning messages will be displayed if you attempt to specify an
invalid task (a task of an invalid type) in the taskmap.
</p>
		</td>
	</tr>
	<tr>
		<td>autowrite</td>
		<td>
<p>
Default: <strong>true</strong>
</p>
<p>
Setting autowrite to false will disable auto insertion using a task
map and the taskmap variable value will be ignored completely if
specified.  All artifact insertion if the build info file will need to
be done explicitly (see examples).
</p>
<p>
Note that setting this variable true will also disable auto generation
of the properties file.  The buildinfo target will need to be
explicitly run or added as a dependency before the property file can
be used in a task.
</p>
<p>
This variable will typically not be used unless you have a specific
need for the info file but do not want to move the file after
generation.  Normally, if you just want the file placed elsewhere and
do not want it included in artifacts, set the filedir/filename
variable to your desired location and filename and set the taskmap to
an empty map (see example below).
</p>
		</td>
	</tr>
</table>

###Examples

To automatically add build info into a zip file in the directory
'testingdir' you can add the specific task to the task map (overriding
the defaults)

```
buildinfo {
	taskmap = [helloZip: "testingdir"]
}

task helloZip(type: Zip) {
	classifier = 'hello'
	from "." include "build.gradle"
}
```

To add the build info file into other files (such as a zip, sync or
other location), you can add your target to the buildinfo taskmap
variable or just do a standard copy as follows.  This is the approach
you would take for tasks that are not derived somehow from a
copy/archive task.

```
task myZip (type: Zip) {
	classifier = 'myZip'
	from(".") {
		include "build.gradle" into "filedir"
	}

	//
	// Add buildinfo
	//
	from (buildinfo.filedir) {
		include buildinfo.filename into 'infodir'
	} 
}
```

To add some custom data to your build info file, add the custom info
map variable to the buildinfo configuration section.  Remember the map
values can be any object at all and the value will be derived from
the default toString() behavior of the object.

```
buildinfo {
	custominfo = [
		"release": releaseType, 
		"version": "${version}.${versionSuffix}", 
		"special": mySpecialVar
	]
}
```

To prevent automatic injection into any tasks, assign an empty map to
the taskmap.

```
buildinfo {
	taskmap = [:]
}
```

To customize the location and name of the build info file use the
filedir and filename variables

```
buildinfo {
	filename = "projectinfo.properties"
	filedir = "${buildDir}/info"
}
```

###Lifecycle Considerations

This plugin hooks task graph completion (which occurs right after the
configuration phase of a gradle run).  At that time, default values
are assigned if not already set.  If the variable autowrite is true,
then and the build info file is created and task hooking is completed
to copy the info file into specified (or default) tasks.

This timing has several specific implications.

- If you are using buildinfo variables in any of your tasks, you need
to ensure the references are done during the execution phase in a task
or that the configuration of your task is accomplished *after* the
configuration of the buildinfo task.  Default assignment of the
buildinfo variables are not done until the configuration phase is
complete and the task graph creation has been completed.

- Any gradle variables used within the buildinfo configuration block must
be assigned prior to the declaration of the buildinfo block.

- Any modification of the buildinfo variables will be ignored once the
configuration phase has been completed and the buildinfo file has been
created.  You can call the buildinfo target directly to recreate the
build info file with new information, but this should be done prior to
any tasks being run which will use the info file for insertion into
artifacts (jar/war/ear/etc) or the risk of inconsistent build
information is present.

###Information sources

Git - This plugin uses the library JGit to obtain git information.
Among other things, this plugin logs the most recent commit
information and the current status (showing modified/delete/added
files).  Development builds can utilize this information to determine
change information for specific artifacts.

Machine - Machine characteristics including username, machine name, IP
address, java vm info and OS info are gathered.

Jenkins - Several key Jenkins variables are stored to shows build id,
url, machine and other important information is stored.

Gradle - Source build file, location and description information is
stored in the information file.

Custom - Any information at all can be specified as a name/value
property set in the gradle build file for important information in
your environment.  New information sources are simple to add in this
plugin if you have an interest in contributing.

##Project Specifics

### Dependencies

This project depends on the following tools currently:

- The gradle API - This is a gradle plugin after all
- JUNIT - There is some JUNIT validation for information sources and
utility code
- JGIT - The git information is acquired using the very well done JGit
project code (used for the Eclipse project)

### Setting Build Version and Build Type

The build version and type is set in the gradle.properties file to
default to 'snapshot.dev'.  This version is made of two parts: the
version and the type (seperated by a period).

Normally the version is set to snapshot.  To build with a particular
version number set the buildVersion property using the gradle command
line (or IDE argument)

```
gradle build -PbuildVersion=1.0.4
```

To build a release version set the buildType property using the gradle
command line (or IDE arguments)

```
gradle build -PbuildType=release
```

##Contributing

I have explicitly built this plugin set for my local technology stack.
Do you like this plugin and just need a new information source, or
have a useful plugin to contribute that surrounds configuration
managment?  I welcome any contributions and pull requests.

##Licensing

<a rel="license" href="http://creativecommons.org/licenses/by/3.0/deed.en_US"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/88x31.png" /></a><br /><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">Gradle CM Plugins</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="https://github.com/kercheval" property="cc:attributionName" rel="cc:attributionURL">John Kercheval</a> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/3.0/deed.en_US">Creative Commons Attribution 3.0 Unported License</a>.<br />Based on a work at <a xmlns:dct="http://purl.org/dc/terms/" href="https://github.com/kercheval/GradleCMPlugin" rel="dct:source">https://github.com/kercheval/GradleCMPlugin</a>.

