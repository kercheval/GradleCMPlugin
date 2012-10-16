#Gradle Build Plugin

This plugin support gradle build information and environment settings.  

The sources here demonstrate the following

- Creation of a gradle plugin using standard Java
- Use of a property to distinguish between versions and build types
- Obtains Git repository information about the build
- Obtains build machine information and user info
- Obtains Jenkins CI build system information


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

