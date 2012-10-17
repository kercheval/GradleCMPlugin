#Gradle Build Plugin

This plugin support gradle build information and environment settings.  

The sources here demonstrate the following

- Creation of a gradle plugin and task using standard Java
- Use of a property to distinguish between versions and build types
- Obtains Git repository information about the build
- Obtains build machine information and user info
- Obtains Jenkins CI build system information
- Obtains Gradle property information

***

<a rel="license" href="http://creativecommons.org/licenses/by/3.0/deed.en_US"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/88x31.png" /></a><br /><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title">Gradle CM Plugins</span> by <a xmlns:cc="http://creativecommons.org/ns#" href="https://github.com/kercheval" property="cc:attributionName" rel="cc:attributionURL">John Kercheval</a> is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/3.0/deed.en_US">Creative Commons Attribution 3.0 Unported License</a>.<br />Based on a work at <a xmlns:dct="http://purl.org/dc/terms/" href="https://github.com/kercheval/GradleCMPlugin" rel="dct:source">https://github.com/kercheval/GradleCMPlugin</a>.

***

## Setting Build Version and Build Type

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

