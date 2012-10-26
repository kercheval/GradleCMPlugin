#Gradle Build Plugin

This plugin supports gradle build information and environment settings.  

## Summary

The sources here demonstrate the following

- Creation of a Gradle plugin and task using standard Java
- Automatic task run based on gradle lifecycle.
- Use of Gradle API for task customization and iteration
- Use of a property to distinguish between versions and build types
- Obtains Git repository information about the build
- Obtains build machine information and user info
- Obtains Jenkins CI build system information
- Obtains Gradle property information

##Usage

```
TODO
- Add custom properties that will go into buildinfo.properties
- Add documentation and examples for autowrite and copy task extension usage

- Can auto target any copy task including copy, sync, tar, zip, jar, war, ear
```

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

