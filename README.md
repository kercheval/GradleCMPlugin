#Gradle Build Plugin

This plugin supports gradle build information and environment settings.  

## Summary

This project is a set of plugins intended to support standard
configuration management practices that are not necessarily well
supported in gradle.  I intend for this source code base to be
instructional as well as useful.

The sources here demonstrate the following

- Creation of Gradle plugins and tasks using standard Java
- Automatic task run based on the gradle lifecycle.
- Use of the Gradle API for task customization and iteration
- Use of the Gradle API for hooking existing tasks 
- Use of java based closure implementation to extend gradle tasks
- Use of a property to distinguish between versions and build types

##Usage

###buildinfo plugin

####Summary

The buildinfo plugin supports the creation of a file (in standard Java
properties format) that shows environment and build information
present at the time a build takes place.  The primary information
gathered is displayed below.

- Obtains Git repository information about the build
- Obtains build machine information and user info
- Obtains Jenkins CI build system information
- Obtains Gradle property information

In addition to the information above, the buildinfo configuration
block can be used to add custom information to the build file.

####Quick Start

Add a buildscript section for the plugin dependency in your build
gradle file.  Note that the example below will take the most recent
released plugin jar file available.

```
buildscript {
    repositories {
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

####Variables

By default, the plugin will create the information file in the default
build directory, name the file buildinfo.properties and insert this
file into the META-INF directory of all created JAR/WAR/EAR files
created in the build.  All of these behaviors are modifiable by
setting custom variables in your gradle build file in the 'buildinfo'
task configuration.  

```
buildinfo {
	filename = "projectinfo.properties"
	filedir = "${buildDir}/info"
}
```

The variables supported are described below.

<table>
	<th>
		<td>Variable</td>
		<td>Description</td>
	</td>
	<tr>
		<td>filename</td>
		<td>
Default: **buildinfo.properties**
		</td>
	</tr>
	<tr>
		<td>filedir</td>
		<td>
Default: **${buildDir}
		</td>
	</tr>
	<tr>
		<td>custominfo</td>
		<td>
Default: **no default info defined**
		</td>
	</tr>
	<tr>
		<td>taskmap</td>
		<td>
Default: **[jar: "META-INF", war: "META-INF", ear: "META-INF"]**

Must be of type AbstractCopyTask (such as copy, sync, tar, zip, jar,
war, ear) to use auto task map.

Warning messages will be displayed if you attempt to specify an invalid task
in the taskmap.
		</td>
	</tr>
	<tr>
		<td>autowrite</td>
		<td>
Default: **true**

Setting autowrite to false will disable auto insertion using a task
map and the taskmap variable value will be ignored.
		</td>
	</tr>
</table>

####Examples


To add the build info file into other files (such as a zip, sync or
other location), you can add your target to the buildinfo taskmap
variable or just do a standard copy as follows.

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

To automatically add build info into a zip file in the directory
'testindir' you can add the specific task to the task map (overriding
the defaults)

```
buildinfo {
	taskmap = [helloZip: "testingdir"]
	custominfo = ["foo": "bar", "baz": "quux", "special": mySpecialVar]
}

task helloZip(type: Zip) {
	classifier = 'hello'
	from "." include "build.gradle"
}
```

####Lifecycle Considerations

- task graph completion for task insertion
- buildinfo variable usage only after definition or use during task
itself

##Project Specifics

### Dependencies

This project depends on the following currently:

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

##Licensing

<a rel="license" href="http://creativecommons.org/licenses/by/3.0/deed.en_US"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/88x31.png" /></a><br /><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">Gradle CM Plugins</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="https://github.com/kercheval" property="cc:attributionName" rel="cc:attributionURL">John Kercheval</a> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/3.0/deed.en_US">Creative Commons Attribution 3.0 Unported License</a>.<br />Based on a work at <a xmlns:dct="http://purl.org/dc/terms/" href="https://github.com/kercheval/GradleCMPlugin" rel="dct:source">https://github.com/kercheval/GradleCMPlugin</a>.

