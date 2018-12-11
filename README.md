# Spring HATEOAS HAL SUPPORT [![Build Status](https://build.fjobilabs.de/job/spring-hateoas-hal-support/job/master/badge/icon)](https://build.fjobilabs.de/blue/organizations/jenkins/spring-hateoas-hal-support/)

Some utilities to improve the HAL support of the Spring HATEOAS project. Especially the support for
embedded resources is much more easier with this library.


## Release Note

Version 0.1.0 released because library is now used in other project and we need to start versioning.

## Maven Dependecy

1. Add FJOBI Labs repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>fjobilabs-snapshots</id>
        <name>FJOBILabs Snapshots</name>
        <url>https://repo.fjobilabs.de/repository/maven-snapshots/</url>
    </repository>
    <repository>
        <id>fjobilabs-releases</id>
        <name>FJOBILabs Releases</name>
        <url>https://repo.fjobilabs.de/repository/maven-releases/</url>
    </repository>
</repositories>
```

2. Add dependecy:

```xml
<dependency>
    <groupId>de.fjobilabs</groupId>
    <artifactId>spring-hateoas-hal-support</artifactId>
    <version>0.1.0</version>
</dependency>
```