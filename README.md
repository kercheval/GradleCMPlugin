#Gradle Configuration Management Build Support

## Project Summary

This project is a set of plugins intended to support standard
configuration management practices that are not necessarily well
supported in gradle.  FAQs, Examples (cookbook) and Overview
discussions can be found on the wiki at
[https://github.com/kercheval/GradleCMPlugin/wiki](https://github.com/kercheval/GradleCMPlugin/wiki).

The [GradleCM Plugin](#gradlecm-plugin) is a simple plugin that
applies all the plugins that are a part of this plugin package.

The [Build VCS Plugin](#build-vcs-plugin) supports interaction with
your local revision control system.  This plugin exposes methods to
determine status, branch names and tags in your project.  This plugin
is used by many of the other plugins in this set of plugins.

- [Quick Start](#build-vcs-quick-start)
- [Variables for `buildvcs`](#build-vcs-variables)
- [Methods for `buildvcs`](#build-vcs-methods)
- [Examples](#build-vcs-examples)
- [Security and SSH Keys](#security-considerations)

The [Build Info Plugin](#build-info-plugin) supports creation of a build time
info properties file which is a part of the build artifacts.

- [Quick Start](#build-info-quick-start)
- [Variables for `buildinfo`](#build-info-variables)
- [Examples](#build-info-examples)

The [Build Version Plugin](#build-version-plugin) supports the tracking,
update and tagging for version numbers in your project and artifacts.

- [Quick Start](#build-version-quick-start)
- [Variables for `buildversion`](#the-buildversion-task)
- [Variables for `buildversiontag`](#the-buildversiontag-task)
- [Examples](#build-version-examples)

The [Build Release Plugin](#build-release-plugin) supports the
maintenance of a release branch and hooks into the publication tasks
for gradle to ensure correct source merge and version tagging when
doing a release artifact publication.

- [Quick Start](#build-release-quick-start)
- [Variables for `buildreleaseinit`](#the-buildreleaseinit-task)
- [Variables for `buildreleasemerge`](#the-buildreleasemerge-task)
- [Variables for `buildrelease`](#the-buildrelease-task)
- [Examples](#build-release-examples)

To use these plugins, add a buildscript section for the plugin
dependency in your build gradle file.  Note that the example below
will take the most recent released plugin jar file available.

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

**Note:** In this documentation, I have attempted to give many examples
that are useful and usually reasonable.  I use the long form of
display such as

```
buildinfo {
	autowrite = false
}
```

rather than the shorter (and equally acceptable) form such as

```
buildinfo.autowrite = false
```

This is for consistency and simplicity.  You may choose one form or
another depending on your preferences and needs.

##GradleCM Plugin

After ensuring the plugin is in your script dependencies, add an apply
line to your gradle build file.

```
apply plugin: 'gradlecm'
```

This will cause all plugins from this plugin set to be applied in the
gradle file and will result in default behaviors (suggested).  All
plugin variables are accessed as described in the plugin specific
sections below.

Note:  If you apply the gradlecm plugin, you need not apply any of the
following plugin as described in their quick summary sections.

##Build VCS Plugin

###Summary

The buildvcs plugin supports build script and plugin integration to
your version control system.  This plugin has a task which is present
purely to allow a simple introduction into the gradle namespace and to
set a variable to determine the type of VCS in use for your
environment.

The `buildvcs` task accomplishes no work and should never be called.

###Build VCS Quick Start

After ensuring the plugin is in your script dependencies, add an apply
line to your gradle build file.

```
apply plugin: 'buildvcs'
```

The methods of the buildvcs plugin are immediately available for use
and do not require the execution of the buildvcs task.

###Build VCS Variables

This plugin supports the following variables:

<table style="border: 1px solid black;">
	<tr>
		<th>Variable</td>
		<th>Description</td>
	</tr>
	<tr>
		<td>type</td>
		<td>
<p>
Default: <strong>git</strong>
</p>
<p>
This variable is set to the supported VCS type for the workspace.  The
VCS type is validated at assignment to ensure a valid type was
specified.
</p>
<p>
The special type 'none' may be used to specify that no version control
system is in use.  This type will disable the use of the buildrelease
plugin and the tagging functionality of the buildversion plugin, but
will still allow use of the build info and version portion of the
buildversion plugin.
</p>
		</td>
	</tr>
</table>

###Build VCS Methods

The buildvcs plugin exposes several useful methods that are available
to your script.

**String buildvcs.getType()** - This method returns the current VCS type.  This
is an alternate form if the variable referenced at buildvcs.type.

**boolean buildvcs.isClean()** - This method returns true if the
current workspace is clean.  This means that there are no modified,
delete or added files in the system (staged or not).  This method
allows the validation of the workspace prior to starting a process
that would not be appropriate with changes present in the system (like
release build uploads).  This method will return true if the
buildvcs.type value is set to 'none'.

**String buildvcs.getBranchName()** - This method returns the current
workspace branch name.  This method is useful for validation or for
use on variable and comment creation.  This method will return a
VCSException if the buildvcs.type value is set to 'none'.

**List<VCSTag> buildvcs.getAllTags()** - This method will return all
tags in the VCS system.  The list elements are of type
org.kercheval.gradle.vcs.VCSTag.  This method will return an empty
list if the buildvcs.type value is set to 'none'.

**List<VCSTag> buildvcs.getTags(String regex)** - This method will
return all tags that match a particular regular expression.  This
method is used to obtain branch and string specific tags for
particular uses.  The buildversion plugin uses this method to obtain
tags based on the version patterns.  This method will return an empty
list if the buildvcs.type value is set to 'none'.

**VCSStatus buildvcs.getStatus()** - This method returns an extended
status for the current workspace.  This method returns an object of
type org.kercheval.gradle.vcs.VCSStatus which can be used to determine
specific files that are modified, delete, changed and staged.  The
isClean() method uses this methods return object to report a clean
state for the workspace.  This method will return an empty
status if the buildvcs.type value is set to 'none'.

**Properties buildvcs.getInfo()** - This method returns a Properties
object that contains the VCS properties used by the `buildinfo` task.
This method will return an empty Properties object if the
buildvcs.type value is set to 'none'.

###Build VCS Examples

**Example 1** To disable the use of vcs by the gradlecm plugin set
(used for standalone project without VCS or using a VCS which is not
supported by this plugin).

```
buildvcs {
	type = "none"
}
```

**Example 2** To explicitly set the type of VCS in use by this plugin
and workspace.

```
buildvcs {
	type = 'git'
}
```

**Example 3** To use the branch name as a basis for part of the
version string.

```
def branchName = buildvcs.getBranchName()

buildversion {
	version.pattern = "%M%.%m%-${branchName}"
}
```

**Example 4** To prevent an action based on if the current workspace
is clean.

```
task myTask << {
	if (buildvcs.isClean()) {
		// Do something interesting
	}
}
```

###Security Considerations

Somewhat independent of this plugin is the topic of remote repository
security.  This description largely surrounds the specifics of using
github, but the areas around SSH key usage are applicable to other VCS
systems you may use.  In very broad terms, the ability to access a
remote repository is independent of this plugin and any remote access
should be secured and tested independently.

####HTTPS vs SSH

In github, the difference between HTTPS and SSH resides largely in the
authentication method.  The HTTPS method requires the specification of
your login credentials for github but with SSH, you use an SSH key
which give access to your github repositories.  Note that the SSH key
approach does not allow admin changes or access to your account, just
the repositories.  In general, SSH is also more secure and you should
clone your remotes using the SSH URI available from github.

####Using Environment Variables

Normally, this plugin will ask interactively if a username, password
or passphrase is required.  Since the intended target for this plugin
is to enable automation and continuous integration, there is support
to supply these values in the environment.

Users of GIT:

If you are using HTTPS, you can use the variables GIT_ORIGIN_PASSWORD
and GIT_ORIGIN_USERNAME:

```
set GIT_ORIGIN_USERNAME=<gitusername>
set GIT_ORIGIN_PASSWORD=<gitpassword)
```

Each build server has its own method for setting environment
variables.  This approach is not recommended since it places github
administration account credentials in the system in clear text.

A better approach is to use SSH and a repository specific deploy key.
If you have a passphrase on your SSH key, you can set the passphrase
in the environment using GIT_ORIGIN_PASSWORD.

```
set GIT_ORIGIN_PASSWORD=<gitkeypassphrase>
```

Since you are exposing the passphrase in cleartext in this instance
anyway, you should also consider using a key without a passphrase for
your build system deploy keys to avoid the need for these variables at
all.

####SSH Keys

To ensure you are using the correct SSH key, a key must be generated
and installed in github.  This is done by the current github clients
automatically, but you can do it in a number of other ways (see
https://help.github.com/articles/generating-ssh-keys).

The main thing you must ensure when setting up your system is to add
the github host to your <home>/.ssh/config file (see
http://en.wikibooks.org/wiki/OpenSSH/Client_Configuration_Files for
exhaustive details).

Inside of the config file, add a section that looks similar to this

```
Host github.com
    User githubusername
    Hostname github.com
    PreferredAuthentications publickey
    IdentityFile ~/.ssh/github_rsa
```

You may have multiple IdentityFile blocks if you have deploy keys that
are repository specific (like the following)

```
Host github.com
    User githubusername
    Hostname github.com
    PreferredAuthentications publickey
    IdentityFile ~/.ssh/repo1_rsa
    IdentityFile ~/.ssh/repo2_rsa
    IdentityFile ~/.ssh/repo3_rsa
```

Key generation is straight forward, but environment specific.  On
windows, I would recommend puttygen
(http://www.chiark.greenend.org.uk/~sgtatham/putty/download.html) and
copy/paste from the application for the public deploy key and use the
conversion menu to export the private key in standard form.

Connect to your origin directly using ssh to ensure the vcs host is
added to your known hosts file (this stores the host and allowed
fingerprint key of the host).  In git, you can just show the remote to
validate this all works

```
git remote show origin
```

##Build Info Plugin

###Summary

The buildinfo plugin supports the creation of a file (in standard Java
properties format) that shows environment and build information
present at the time a build takes place.  The primary information
gathered includes:

- Git repository information about the build
- Build machine information and user info
- Jenkins CI build system information
- Hudson CI build system information
- TeamCity CI build system information
- Gradle property information

In addition to the information above, the `buildinfo` configuration
block can be used to add custom information to the build file.

###Build Info Quick Start

After ensuring the plugin is in your script dependencies, add an apply
line to your gradle build file.

```
apply plugin: 'buildinfo'
```

This will cause a file called buildinfo.properties to be placed within
your ${buildDir} directory and will automatically insert the
buildinfo.properties file into the META-INF directory of all generated
jar, war and ear files.

###Build Info Variables

Most behaviors of this plugin are modifiable by setting custom
variables in your gradle build file in the `buildinfo` task
configuration as illustrated in the examples below.

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
Default: <strong>${buildDir}</strong>
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
The taskmap variable holds a map of tasks and the directory in the
tasks target file that the build info file will be copied into.
If the info file should be copied to the root of the target, use an
empty string as the target directory.
</p>
<p>
Note: when creating a taskmap variable, you are overriding the
default values.  If you intend to simply add another task, you will
need to supply the default values for jar/war/ear tasks in your
taskmap.  This behavior ensures you can override all the default
behaviors.  Note that if you just want the info file built
automatically but no task modification at all, you can set the
taskmap to an empty map `[:]` and no tasks will be modified.
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
		<td>showgradleinfo</td>
		<td>
<p>
Default: <strong>true</strong>
</p>
<p>
Normally, some gradle environment state is placed in the info file.
If this variable is set to false, that information will not be placed
in the file.
</p>
		</td>
	</tr>
	<tr>
		<td>showmachineinfo</td>
		<td>
<p>
Default: <strong>true</strong>
</p>
<p>
Normally, current machine and login information is place in the info
file.  This information will not be placed in the file is this
variable is set to false.
</p>
		</td>
	</tr>
	<tr>
		<td>showvscinfo</td>
		<td>
<p>
Default: <strong>true</strong>
</p>
<p>
If this variable is set to false, the current VCS status will not be
added to the info file.
</p>
		</td>
	</tr>
	<tr>
		<td>showciinfo</td>
		<td>
<p>
Default: <strong>true</strong>
</p>
<p>
Normally, any detected continuous integration build system (Jenkins,
Hudson or TeamCity) will trigger some information output in the info
file.  This detection and information placement will not take place if
this variable is set to false.
</p>
		</td>
	</tr>
	<tr>
		<td>showtaskinfo</td>
		<td>
<p>
Default: <strong>true</strong>
</p>
<p>
The info file will normally show the tasks that were active in the
task graph at the time the build info file was created.  Setting this
variable to false will prevent that information from being placed in
the file.
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
of the properties file.  The <code>buildinfo</code> task will need to be
explicitly run or added as a dependency before the property file can
be used in a task.
</p>
<p>
This variable will typically not be used unless you have a specific
need for the info file but do not want to add the file into your
generated artifacts after generation.  Normally, if you just want the
file placed elsewhere and do not want it included in artifacts, set
the filedir/filename variable to your desired location and filename
and set the taskmap to an empty map (see example below).
</p>
		</td>
	</tr>
</table>

###Build Info Examples

**Example 1** To automatically add build info into a zip file in the directory
'testingdir' you can add the specific task to the task map (overriding
the defaults).  In this example we preserve the copy of
buildinfo.properties into newly created jar files.

```
buildinfo {
	taskmap = [
		helloZip: "testingdir",
		jar: "META-INF"
	]
}

task helloZip(type: Zip) {
	classifier = 'hello'
	from "." include "build.gradle"
}
```

**Example 2** To add the build info file into other files (such as a
zip, sync or other locations), you can add your task to the
buildinfo taskmap variable or just do a standard copy as follows.
This is the approach you would take for tasks that are not derived
somehow from a copy/archive task.

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

**Example 3** To add some custom data to your build info file, add the custom info
map variable to the `buildinfo` configuration section.  Remember the map
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

**Example 4** To prevent automatic injection into any tasks, assign an empty map to
the taskmap.

```
buildinfo {
	taskmap = [:]
}
```

**Example 5** To customize the location and name of the build info file use the
filedir and filename variables

```
buildinfo {
	filename = "projectinfo.properties"
	filedir = "${buildDir}/info"
}
```

**Example 6** To prevent the current machine information from being
placed in the output file.

```
buildinfo {
	showmachineinfo = false
}
```

**Example 7** To support multi-project environments it is often
simpler to disable the auto write support using the task map and
instead use a closure to get all tasks of a particular type.

This approach also has the advantage of not adding the buildinfo
file into the inputs collection and thus jar/war/ear files will be
updated with a new build info file only when a contributing source
file is modified and not on every build.

```
buildinfo {
    //
    // The buildinfo file will be placed in jars via a doFirst enclosure
    // for all subprojects.  Disable auto insertion via the task map.
    //
    taskmap = [:]
}

subprojects {
    //
    // Manifest specific properties.  All tasks that inherit from jar
    // (war/ear) are also affected by this
    //
    tasks.withType(Jar) {
        //
        // Place buildinfo into all jar files
        //
        doFirst {
            from(rootProject.buildinfo.filedir) {
                include rootProject.buildinfo.filename
                into 'META-INF'
            }
        }
    }
}
```

###Lifecycle Considerations

This plugin hooks task graph completion (which occurs right after the
configuration phase of a gradle run).  If the variable 'autowrite' is
true, then the build info file is created and task hooking is
completed to copy the info file into specified (or default) tasks.

This timing has several specific implications.

- Any gradle variables used within the `buildinfo` configuration block must
be assigned prior to the declaration of the `buildinfo` block.

- Any modification of the `buildinfo` variables will be ignored once the
configuration phase has been completed and the build info file has been
created.  You can call the `buildinfo` task directly to recreate the
build info file with new information, but this should be done prior to
any tasks being run which will use the info file for insertion into
artifacts (jar/war/ear/etc) or the risk of inconsistent build
information is present.

- The gradle default `clean` task explicitly removes the build
directory artifacts.  The default location for the
buildinfo.properties file is in this directory.  If you use the
`clean` task with other tasks that build artifacts you will need to
ensure the `buildinfo` task is specified after the `clean` task.  For
example, the following gradle command line will run the `clean` task and
then the `buildinfo` task, then build the project artifacts.

```
gradle clean buildinfo build
```

As an alternative, you could place the buildinfo.properties file in a
location that is not cleaned by the default `clean` task.

###Information sources

Git - This plugin uses the library JGit
(<http://www.eclipse.org/jgit/>) to obtain git information.  Among
other things, this plugin logs the most recent commit information and
the current status (showing modified/delete/added files).  Development
builds can utilize this information to determine change information
for specific artifacts.

Machine - Machine characteristics including username, machine name, IP
address, java vm info and OS info are gathered.

Jenkins - Several key Jenkins variables are stored to shows build id,
url, machine and other important information.

Hudson - Several key Hudson variables are stored to shows build id,
url, machine and other important information.

TeamCity - Several key TeamCity variables are stored to shows build id,
data and other important information.

Gradle - Source build file, location and description information is
stored in the information file.

Custom - Any information at all can be specified as a name/value
property set in the gradle build file for important information in
your environment.  New information sources are simple to add in this
plugin if you have an interest in contributing.

##Build Version Plugin

###Summary

The buildversion plugin supports the automatic setting of build numbers
based on VCS tag labeling.  The plugin supports multiple branch
versioning and can be used without tagging at all.

Using the default behavior of the plugin, the gradle version object
will be updated to reflect a single increment version update from the
last tag recognized as a tag label.  For example, if a tag was found
with version 2.6, the gradle version would be updated to 2.7 (your
builds are actually targeted at the next version release, not the last
one).

The increment behavior, build numbers and version format default
behaviors can all be overriden using the task variables.

There are two tasks defined in this plugin:

####buildversion

This task will find the most recent (not the highest build number, but
the most recently placed) version tag and will use that as the
template for the build version.  The tags used for comparison are
filtered based on the validatePattern set in the configuration block
(see the variable section).  The gradle version update occurs at the
point at which the task graph is completed.  This is just after the
evaluation phase of the build and just prior to actual task execution.

Nearly any version scheme you may want to utilize is supported by this
plugin.  Some common version schemes shown in the examples are:

- M.m-d.t (standard maven versioning - the default)
- M.m.b (classic major/minor/build)
- Y.m.b (year/minor/build)

Extended patterns are supported to allow very flexible variations.

####buildversiontag

This task will take the current version and write a tag using the
current version format.

If this task is used directly, then this tag is written to only the
local repository for the vcs in use.  In this case, if you wish these
tags published in a central repository, you will need to push the tags
to the origin repository explicitly (this would be 'git push origin
--tags' for git users).

The `buildversiontag` task always depends on the `buildversion` task
and will use variables created in the `buildversion` configuration
block for tag output.

The tag and comment inserted into VCS will be output when the --info
command line option is used on the gradle command line.

###Build Version Quick Start

After ensuring the plugin is in your script dependencies, add an apply
line to your gradle build file.

```
apply plugin: 'buildversion'
```

This will automatically cause the tag list to be parsed and the
version object to be placed in project.version.

The `buildversiontag` task must be executed seperately by an explicit
gradle call

```
> gradle buildversiontag
```

or by setting the dependsOn property of another task to be
run.

```
task doTag(dependsOn: buildversiontag) << {
	println 'Hello from doTag'
}
```

###Build Version Variables

####The `buildversion` task

The primary output of the `buildversion` task is to place an
org.kercheval.gradle.buildversion.BuildVersion object as the
project.version object.  This object can be referenced by using
`project.version` or `buildversion.version`.

The `buildversion` task behavior can be modified by the following
variables:

<table style="border: 1px solid black;">
	<tr>
		<th>Variable</td>
		<th>Description</td>
	</tr>
	<tr>
		<td>autoincrement</td>
		<td>
<p>
Default: <strong>true</strong>
</p>
<p>
When autoincrement is set to true, the version build number will be
incremented when set.  For example if the version was derived to be
2.6.3, the version when set will be 2.6.4.  Note that the most
volatile value in the version will be incremented with the order being
to change the build number if enabled, otherwise the minor number is
modified if enabled, otherwise the major version is modified.  The
current date for the version is always updated on increment.
</p>
<p>
If autoincrement is false, the version number used will be the
evaluated version which came from the tag list or from the
configuration settings.  Tasks can always use the incrementVersion()
method of the version variable to accomplish this action (reasonable
when creating tags with autoincrement off).
</p>
		</td>
	</tr>
	<tr>
		<td>usetag</td>
		<td>
<p>
Default: <strong>true</strong>
</p>
<p>
If the usetag variable is set to true, the version is determined based
on the most recent tag which matches the version pattern.  If a tag
matching the pattern does not exist, then the values initially set in
the version variable will be used (these values all default to zero).
See the version variable for details on the pattern set and default
values.  When set to false, the `buildversion` task will not do any tag
list evaluation at all and will only use the values set in the
`buildversion` configuration block.
</p>
		</td>
	</tr>
	<tr>
		<td>version</td>
		<td>
<p>
Default: <strong>org.kercheval.gradle.buildversion.BuildVersion(null,
0, 0, 0, null)</strong>
</p>
<p>
This variable is of type
org.kercheval.gradle.buildversion.BuildVersion.  This object holds the
version state as well as the creation and parsing patterns used to generate the
version string.  Along with maintaining major, minor and build
numbers (any or all of which can be used), this object also maintains
a build date (which can also be used in the version string).
</p>
<p>
The actual version string creation is controlled by the use of a
creation pattern.  The pattern used has the following restrictions
</p>
<ul>
<li>May not have any whitespace (validated)</li>
<li>May contain any of the following variables (at most once)
<ul>
<li>%M% - major version</li>
<li>%m% - minor version</li>
<li>%b% - build number</li>
<li>%d% - date (using yyyyMMdd)</li>
<li>%t% - time (using HHmmss)</li>
<li>%% - a percent character (may appear multiple times in the pattern)</li>
</ul>
</li>
</ul>
<p>
The pattern can otherwise have any valid form to create patterns that
are specific to your needs or are specific to your branch or build
type.  The default pattern used if no pattern is explicitly set is
"%M%.%m%-%d%.%t%".  Invalid patterns will result in a build failure.
</p>
<p>
In addition to the version pattern, you can set a validation Pattern.
This pattern is a regular expression following standard Java regex
patterns and is used to filter tags on version parse as well as
internally for verification of output version strings.  The validation
pattern enables the use of the version plugin in branch specific
contexts (meaning multiple vcs branches can all be using different
versions for building artifacts) and for special tag list filtering
when setting the initial version.  If a validation pattern is not
explicitly set, then a validation pattern is automatically generated
from the version pattern.
</p>
<p>
The validation pattern must always be consistent with the version
pattern.  To set these patterns, you can use the setPattern() method
described below and shown in the examples section.
</p>
<p>
The version.major, version.minor, version.build and version.buildDate
variables can all be set in the configuration section.  If the usetag
variable is set, you should set these variables in a doLast closure as
shown in the examples section.
</p>
<p>
There are a few useful methods that can be used on the version object.
Normally, these methods should be used only after the initial
`buildversion` task has been run.  This is most easily accomplished by
the use of a doLast closure in the configuration block for
`buildversion` (see examples) or usage in any task run after the
`buildversion` task.
</p>
<p>
<strong>version.setPattern(String pattern)</strong> - This method will set the version
pattern to a new value and autogenerate a validation pattern.  You can
use this method to set an initial value for initialization and then
use it again to modify the version output for specific types of builds
(such as development or snapshot builds).
</p>
<p>
<strong>version.setPattern(String pattern, String validatePattern)</strong> -
This method is used to set both a version pattern and a
validatePattern.
</p>
<p>
<strong>version.incrementVersion()</strong> - This method will increment the
most volatile value in the current version pattern.  For instance if
your pattern was "%M%.%m%", then the minor version would be
incremented and if your pattern was "%M%.%m%.%b%" then the build
number would be incremented.
</p>
<p>
<strong>version.updateMajor(int newMajor)</strong> - This method will
set the major version and if different than the current major version
will reset the minor version to 0.  This behavior simplifies the
gradle script logic surrounding version updates.
</p>
<p>
<strong>version.incrementMajor()</strong> - This method will increment the
major version number.  This method will reset the minor version to 0.
</p>
<p>
<strong>version.incrementMinor()</strong> - This method will increment the minor
version number.
</p>
<p>
<strong>version.incrementBuild()</strong> - This method will increment the
build version number.
</p>
<p>
<strong>version.toString()</strong> - The standard toString() method will
generate a revision string that is created based on the current
pattern.  The newly created revision string is validated using the
validation pattern to ensure consistent behavior.
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
When set to true, the project.version value will be set at task graph
completion (just before tasks are executed and just after the
configuration phase).  This is normally exactly the right behavior,
but specific build ordering or other custom needs may be require some
form of later task execution (and thus late project.version binding).
</p>
		</td>
	</tr>
</table>

####The `buildversiontag` task

By default the `buildversiontag` task will generate a tag in the current
branch of your VCS using the pattern for the version specified by the
`buildversion` task.  Normally, this will only be allowed when the
current workspace is considered clean (no modified/added/deleted
files).  The intent of the written tag is to represent a reproducible
build point so the tag will be attached to the current checkout commit
(usually the HEAD in git).

The `buildversiontag` task behavior can be modified by the following
variables:

<table style="border: 1px solid black;">
	<tr>
		<th>Variable</td>
		<th>Description</td>
	</tr>
	<tr>
		<td>comment</td>
		<td>
<p>
Default: <strong>"Tag created by task buildversiontag"</strong>
</p>
<p>
The comment variable determines the comment or description field for
the generated tag.  This field is not used for determination of
version or functionality in any way, but is a useful way to place
build information of interest into the tag.
</p>
		</td>
	</tr>
	<tr>
		<td>onlyifclean</td>
		<td>
<p>
Default: <strong>true</strong>
</p>
<p>
When the onlyifclean variable is set to true, the build will fail if
the `buildversiontag` task is run when the current workspace is not
clean.  To be clean, all modified/added/deleted files must be
committed and the current workspace must represent a specific commit
of the vcs system (note it is NOT necessary that the tag be at the
current head).
</p>
<p>
When set to false, tags will be allowed to be written at any time, but
the tag will be attached to a commit that may have outstanding changes.
This means that the current build artifacts on the current location
may not match the artifacts later created based on the tag since there
were changed files on this build workspace at the time the tag was
written.  This is typically not a good practice and this variable
should normally remain true.
</p>
		</td>
	</tr>
</table>

###Build Version Examples

**Example 1** To prevent the version from auto incrementing so that the version
reflects the last tag value (rather than the 'next' version).

```
buildversion {
	autoincrement = false
}
```

**Example 2** To set a specific major version after the initial revision
has been obtained from tags.  Note the use of the doLast closure
(should be used anytime you are explicitly setting a value when usetag
is true).  Note that this example could use the updateMajor() method
to accomplish this as well.  This example sets the major version but
does not affect the minor version at all.

```
buildversion {
	doLast {
		version.major = 2;
	}
}
```

**Example 3** To use a specific version number that is controlled only by gradle
variables (this example will result in version 3.3).  (Use autoincrement
set to false to have the version match exactly).

```
buildversion {
	usetag = false;
	version.major = 3
	version.minor = 2
}
```

**Example 4** To use a 'classic' major.minor.build version scheme that is set via
gradle variable usage.

```
buildversion {
	usetag = false;
	version.setPattern('%M%.%m%.%b%')
	version.major = 3
	version.minor = 2
	version.build = buildNumber // perhaps via Jenkins build id?
}
```

**Example 5** To create a version string that has only a major and minor version

```
buildversion {
	version.pattern = '%M%.%m%'
}
```

**Example 6** To create a branch specific version pattern

```
def currentBranch = 'mainline'

buildversion {
	version.setPattern("%M%.%m%-${currentBranch}")
}
```

**Example 7** To create a validation pattern and version pattern to
create a branch specific version (useful for hotfix branches, parallel
development, etc.) but will grab the most recent tag from any branch
(named without numbers).  Note that the regex can be arbitrarily
complex so you can do any sort of tag filtering you want as long as it
is consistent with the version pattern.

```
def currentBranch = 'release'

buildversion {
	version.setPattern("%M%.%m%-${currentBranch}", "\\d+.\\d+-\\D+")
}
```

**Example 8** To explicitly increment the version in a task through the project
variable (note the << is the same as using a doFirst closure)

```
task doIncrementBeforeAction << {
	project.version.incrementVersion();
}
```

**Example 9** To set a comment for the tag created by the
`buildversiontag` task

```
buildversiontag {
	comment = 'This is a comment in build.gradle for buildversiontag'
}
```

**Example 10** To allow tags to be generated even when the workspace has uncommitted
changes.

```
buildversiontag {
	onlyifclean = false
}
```

**Example 11** To set a comment and increment the version prior to writing a version
tag.

```
buildversiontag {
	comment = 'This is a comment in build.gradle for buildversiontag'
	doFirst {
		version.incrementVersion()
	}
}
```

**Example 12** To create a version based on the year, a minor version and
the current build number (ie r2012.1.345)

```
def year = Calendar.getInstance().get(Calendar.YEAR);

buildversion {
	version.setPattern('r%M%.%m%.%b%')
	doLast {
		//
		// Use updateMajor() to ensure that minor is reset if
		// different (ie if was 2011.5 setting to 2012 will
		// result in 2012.0
		//
		version.updateMajor(year);
	}
}
```

**Example 13** To create a version that generates SNAPSHOT unless the
release branch is in use.  This is an extremely convenient approach
and highly **recommended**.

```
def buildMajorVersion=1
buildversion {
	doLast {
		//
		// Set the pattern after the tags have been used to set the initial
		// values.  Release gets the default pattern of the maven default
		//
		def branchName = buildvcs.getBranchName()
        if (branchName != 'release' &&
            !project.hasProperty('noSnapshot')) {
			version.setPattern("%M%.%m%-SNAPSHOT")
		}
		version.updateMajor(new Integer(buildMajorVersion))
		println("Currently working on sources for " + version);
	}
}
```

**Example 14** To create a version based on build type (release does a
full version, but dev mainline creates snapshot builds).  In this
example the major version is part of the configuration file as well.
This has some similarities to the last example.

In gradle.properties set the build type

```
buildType=SNAPSHOT
buildMajorVersion=4
```

You can set the buildtype on the command line to override the default
for CI release builds (or manual release builds)

```
> gradle build -PbuildType=release
```

Within the gradle.build file set the pattern based on the build type
so that you will get versions like 4.3-SNAPSHOT (assuming the last
release version was 4.2), but when doing release builds you get the
full blown 4.3-20111028.123456 revision numbers (including the maven
style date default pattern).  Notice the use of the doLast closure to
init from the last release tag but to use the standard snapshot
version string during the build.  This makes for a very flexible
environment with very simple configuration.

```
buildversion {
	doLast {
		//
		// Set the pattern after the tags have been used to
		// set the initial values.  Release gets the default.
		//
		if (buildType != "release") {
			version.setPattern("%M%.%m%-${buildType}")
		}
		version.updateMajor(new Integer(buildMajorVersion))
	}
}
```

**Example 15** To ensure that on every update to a repository (via the
maven plugin) you get a valid tag in the current branch.  This is done
by adding a doFirst closure to the maven upload task.  The tag is
created whenever you are not doing a snapshot upload in this example.

```
uploadArchives {
	doFirst {
		if (buildType != "SNAPSHOT") {
			tasks.buildversiontag.execute()
		}
	}
}
```

###Lifecycle Considerations

This plugin hooks task graph completion (which occurs right after the
configuration phase of a gradle run).  Note that the version variable
will not be referencable as described in the variable section via the
project until after this task has run.

##Build Release Plugin

###Summary

The buildrelease plugin supports the consistent promotion and
publication of release artifacts.  This plugin maintains a knowledge
of your mainline branch, release branch, remote origin repository and
upload task and ensures that when artifacts are published a consisten
merge occurs to the release branch, a version specific tag is created
and that those changes are pushed to your remote repository prior to
artifact upload.  This plugin also supports these functions for those
using repositories without remote origins (local only development).

This plugin will interact with the local and remote repositories, but
will not change the branch is use for the current workspace.
Repository changes are limited to tag creation and repository
synchronization only.

This plugin is designed to optimize repository use for both
development and build/deploy.  The default development mainline is
assumed to be the 'master' branch and the release branch is assumed to
be the branch named 'release'.  These can be changed in the variables
defined for the `buildreleaseinit` task.

There are three tasks defined in this plugin:

####buildreleaseinit

This task initializes the build environment necessary to use the
release plugin.  This task is typically used explicitly only once for
a project, but the variables defined by this task are used by the
other release plugin components.

At the completion of this task, the release and development branches
will exist in the local and remote repositories.

####buildreleasemerge

This task is responsible for ensuring that all changes from the remote
origin are merged to the local repository and merging the changes
from the development mainline into the release branch.

This task will push the local repository updates and any tags
generated to the remote origin repository.

####buildrelease

This is simple build and release task that has no variables or
custom behavior except that defined by the other release tasks.

###Build Release Quick Start

After ensuring the plugin is in your script dependencies, add an apply
line to your gradle build file.

```
apply plugin: 'buildrelease'
```

This will ensure the upload task specified in the configuration
block for `buildreleaseinit` is hooked to ensure that release artifact
publication results in appropriately updated repositories and tags
being set for the publication.

###Build Release Variables

####The `buildreleaseinit` task

The primary purpose and result of the `buildreleaseinit` task is to
create the branches required for code promotion and artifact
publication.  This task is a dependency for the `buildreleasemerge`
task.

Running this task will synchronize the local and remote repository
branch structure for the two branches in question.  This allows for
clones taking over the role of release delivery and simpler trasfer of
the role of different repositories.

At task startup, there are several possible initial conditions for
each branch:

- Both the local and remote already have the branch (no changes are
made).
- The local has the branch but the remote does not (the remote is
created)
- The remote has the branch but the local does not (the local is
created)
- Both the local and the remote do not have the branch (the branch is
created on both repositories)

In all cases, the result is that both the release and mainline
branches will exist on both the local and remote repositories and will
relate to each other as local and origin.

**Note:** If the local and remote branches are found to exist it is
assumed that they are on related code lines.  No validation is made by
this task to ensure that the same named branches on the remote and
origin are connected.

If this system is being setup without a remote repository this plugin
will operate in a local repository only mode (see the ignoreorigin
variable description)

The buildrelease plugin task behaviors can be modified by the
following variables:

**Note:** These variables are used by all of the buildrelease plugin
tasks (except where noted).

<table style="border: 1px solid black;">
	<tr>
		<th>Variable</td>
		<th>Description</td>
	</tr>
	<tr>
		<td>releasebranch</td>
		<td>
<p>
Default: <strong>release</strong>
</p>
<p>
This represents the release branch line for the build and upload of
release artifacts.  During release artifact upload, it is this branch
that will receive the merge from the mainline and this branch that
will receive the release tag (though in git this distinction has very
little meaning).
</p>
<p>
This variable should normally be different than that used for the mainline
branch (this is not enforced).  If you have a specific reason
to do all development and release on a single branch, this is
supported (though not necessarily recommended).
</p>
<p>
The default name is the natural one for promotion and is, importantly, not
the master branch.  The master branch is the normal default branch for
remote repositories and should explicitly *not* be used for release
activity (the chances for accidental modification are quite high).
</p>
		</td>
	</tr>
	<tr>
		<td>mainlinebranch</td>
		<td>
<p>
Default: <strong>master</strong>
</p>
<p>
This variable represents the primary mainline branch in the system.
This branch is the source of all code promoted to the release branch
for artifact upload and publication.  While this branch may not be the
chosen day to day development branch (depending on your teams needs),
it is the reference source that should normally used for snapshots.
</p>
<p>
The choice of the name 'master' for this branch is intentional.  By
default, most repository clones (in git anyway) will possess this
branch name and that branch will be the default.  Individuals needing to
make changes to your project will typically wish to clone your
repository, make some changes and push back the changes (or create a
pull request) without the overhead or worry of dealing with local
branch/merge strategies.  This is not to say that local branches or
more sophisticated usage is not normal or desirable, just that this
variable default was chosen to make the task of update as simple and
as straightforward as possible for the broadest demographic possible.
</p>
		</td>
	</tr>
	<tr>
		<td>remoteorigin</td>
		<td>
<p>
Default: <strong>origin</strong>
</p>
<p>
This variable represents the remote repository origin.  In git this is
typically called 'origin', but a remote can be pulled in from numerous
sources.  Using a primary remote other than the default origin is not
highly recommended, but supported by this plugin.  You will typically
not change this value.
</p>
		</td>
	</tr>
	<tr>
		<td>ignoreorigin</td>
		<td>
<p>
Default: <strong>false</strong>
</p>
<p>
Single repository workflows are a bit unusual when using a DVCS (like
Git or Mercurial), but there are some use cases for this.  When true,
the init and merge tasks will not attempt to synchronize
repositories with origins (present or not).  When this variable is set
to false, all changes to repositories made by this plugin will be
synchronized with the origin repository (fetch, merge, push and tag
operations).
</p>
		</td>
	</tr>
	<tr>
		<td>fastforwardonly</td>
		<td>
<p>
Default: <strong>true</strong>
</p>
<p>
Auto merging of sources can work extremely well when dealing with
code conflicts, but semantic or logic conflicts can be very problematic.
By default the `buildreleasemerge` task will not merge changes unless
the merge is a fast forward merge (this is true if a merge has been done
from the release branch to the mainline branch).  If a fast forward
merge is not possible, the task will halt the build with an appropriate
message.  If set to false, the `buildreleasemerge` task will attempt
a merge even if fast forward merges are not possible (and will still
correctly fail if a physical conflict occurs).
</p>
		</td>
	</tr>
	<tr>
		<td>uploadtask</td>
		<td>
<p>
Default: <strong>uploadArchives</strong>
</p>
<p>
This variable represents the standard upload task you are using for
publication.  The publication default task for Gradle is
'uploadArchives' and this is the default value for this variable.
</p>
<p>
This plugin does not actually do any publication of artifacts.  You
may be using Ivy or Maven repositories (Nexus, Artifactory, etc) or
you may be publishing to a machine folder or network drive.  The
specific task that you are using for this purpose should be the
value of this variable.
</p>
<p>
As a point of practice, you should not normally need to set this
variable as long as you have configured your uploadArchives
configuration correctly.
</p>
<p>
When the task graph has completed, the upload task is modified to
first perform the following steps:

<ol>
<li>Determine if the workspace is on the release branch.  If the
workspace is not currently on the release branch, just execute the
upload task normally.  If the workspace is on the release branch, then
execute the remaining steps below.</li>
<li>If the variable 'onlyifclean' is set to true, determine if the
workspace is clean.  If the workspace is not clean, build execution
is stopped.</li>
<li>Tag the current branch with a tag named for the current gradle
version (project.version).  The tag comment describes the task that
created the tag (ie. uploadArchives).</li>
<li>If the variable ignoreorigin is false, the tag is pushed to the
origin repository.  Failure to push the tag to the origin will result
in build execution being stopped.</li>
</ol>

<strong>Note:</strong> The task hook is accomplished via a doFirst()
closure.  If other doFirst() closures are created for your upload task
(particularly if they are done dynamically), they may may be executed
prior to tagging and push.  Be aware of this timing if you have
additional customization in your upload task via a doFirst closure.
</p>
<p>
Normally, the task <code>buildreleasemerge</code> should have been run
prior to running the uploadArchive task, but there is no explicit
dependency placed on the uploadArchive task.  Normally, artifact
publication should be done via the use of the
<code>buildrelease</code> task which has appropriately ordered
dependencies to ensure that repository merging occurs before build and
upload.
</p>
		</td>
	</tr>
	<tr>
		<td>onlyifclean</td>
		<td>
<p>
Default: <strong>true</strong>
</p>
<p>
When false, the tag and merge steps in the
<code>buildreleasemerge</code> task
will allow the use of a dirty workspace.  This may be useful if
intermediate files are added during build sequencing that are not part
of your ignore file.  When true, the workspace will verified as being
clean when running the <code>buildreleasemerge</code> task.
</p>
<p>
<strong>Note:</strong> This variable setting will have no affect on
the tagging and push actions associated with the specified upload
task.  Setting this variable to false should be considered an error in
usage normally and is highly discouraged.
</p>
		</td>
	</tr>
</table>

####The `buildreleasemerge` task

This task is at the core of the buildrelease plugin.  When run, the
following actions are taken:

1. Verify the current branch is the release branch.
1. Verify the current workspace is clean.
1. Fetch the origin content (when origin in use)
1. Merge the origin release branch (when origin in use)
1. Merge the mainline branch to the release branch
1. Push the changes to the origin (when origin in use)

Any failure at any step in this sequence will result in build
execution being stopped.

**Note:** The merges both from the remote origin and from the mainline
must be fast forward or no collision merges.  A merge failure at this
point will result in a hard reset of the current branch.  In the event
of a merge or push failure, correction will need to be made manually.
All activity done on the release branch should be manually merged back
into the mainline to prevent problems in this area.

There are no variables supported by this task.

####The `buildrelease` task

This task is combines the merge and upload tasks via a dependsOn
relationship.  This task depends on the buildreleasemerge and the
upload task (defined in buildreleaseinit).  This task simplifies
release semantics (particularly when using CI servers).

The tags generated by the `buildrelease` task in the buildrelease
plugin are automatically pushed to remote origins.

There are no variables supported by this task.

###Build Release Examples

**Example 1** To set the mainline (development) branch to 'dev' and
the release branch to 'prod'.

```
buildreleaseinit {
	mainlinebranch = 'dev'
	releasebranch = 'prod'
}
```

**Example 2** To set the remote origin to 'myOrigin' (for instance, in
a repository with multiple remotes) using the same mainline and
release branch as Example 1.

```
buildreleaseinit {
	mainlinebranch = 'dev'
	releasebranch = 'prod'
	remoteorigin = 'myOrigin'
}
```

**Example 3** To ensure that origins will not be updated (working in a
local repository environment)

```
buildreleaseinit {
	ignoreorigin = true
}
```

**Example 4** To use a custom artifact release task.

```
buildreleaseinit {
	uploadtask = myUploadTask
}
```

**Example 5** To allow merges that are not fast forward only by the
`buildreleasemerge` task.

```
buildreleaseinit {
    fastforwardonly = false
}
```

##Project Specifics

I intend for this source code base to be instructional as well as
useful.

The sources here demonstrate the following

- Creation of Gradle plugins and tasks using standard Java
- Automatic task run based on the gradle lifecycle.
- Use of the Gradle API for task customization and iteration
- Use of the Gradle API for hooking existing tasks
- Use of java based closure implementation to extend gradle tasks
- Use of JGit to obtain VCS status and to read and set tags

### Release History

- 1.15 - Oct 30, 2013 - Complete a TODO to remove workaround for bug corrected in Gradle 1.4.  Update project to use Gradle 1.8 (plugin still supports 1.6+).
- 1.14 - June 11, 2013 - Minor updates to use and support Gradle 1.6.  From this release on, you must use Gradle 1.6+ to use this plugin.
- 1.13 - May 6, 2013 - Minor change to show files involved in clean workspace violations.
- 1.12 - Mar 13, 2013 - Add support for fast forward merge restrictions in `buildreleasemerge` task.
- 1.11 - Jan 20, 2013 - Correct minor issue where tags could possibly be created on mainline when publishing.
- 1.10 - Nov 19, 2012 - Use the JGIT StringUtils variant to avoid dependency on apache commons.
- 1.9 - Nov 17, 2012 - Add support for interactive username/password/passphrase prompting for VCS.
- 1.8 - Nov 16, 2012 - Remove some debug messages in gradle info source.  No functional changes.
- 1.7 - Nov 16, 2012 - Add Hudson and TeamCity support to buildinfo.  Add variables to control info types in buildinfo.properties.
- 1.6 - Nov 15, 2012 - Work around some gradle concurrent modification exceptions when adding dependencies to dynamic tasks (http://issues.gradle.org//browse/GRADLE-2023)
- 1.5 - Nov 13, 2012 - The initial completion of the buildrelease, buildvcs and gradlecm plugins
- 1.0 - Nov 6, 2012 - The initial completion of the buildversion plugin
- 0.6 - Oct 20, 2012 - The initial completion of the buildinfo plugin

### Dependencies

This project depends on the following tools currently:

- The gradle API
(<http://www.gradle.org/docs/current/javadoc/index.html>) - This is a
gradle plugin after all
- JUNIT (<http://www.junit.org/>) - There is some JUNIT validation
for information sources and utility code
- JGIT (<http://www.eclipse.org/jgit/>) - The git information is
acquired using the very well done JGit project code (used for the
Eclipse project)

### Current Steps to Release Artifacts

*Snapshot Upload*

```
> gradle uploadArchive
```

*Release Upload*

```
> git checkout release
> gradle clean
> gradle buildrelease
```

##Contributing

I have explicitly built this plugin set for my local technology stack,
but the intent is that additional support should be simple to add.

Do you like this plugin and just need a new information source, or
have a useful plugin to contribute that surrounds configuration
managment?  I welcome any contributions and pull requests.

John Kercheval (kercheval@gmail.com)

##Licensing

<a rel="license" href="http://creativecommons.org/licenses/by/3.0/deed.en_US"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/88x31.png" /></a><br /><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">Gradle CM Plugins</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="https://github.com/kercheval" property="cc:attributionName" rel="cc:attributionURL">John Kercheval</a> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/3.0/deed.en_US">Creative Commons Attribution 3.0 Unported License</a>.<br />Based on a work at <a xmlns:dct="http://purl.org/dc/terms/" href="https://github.com/kercheval/GradleCMPlugin" rel="dct:source">https://github.com/kercheval/GradleCMPlugin</a>.

