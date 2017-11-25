# Thin Jar Launcher

[![travis-ci](https://api.travis-ci.org/develrulez/thin-jar-launcher.svg)](https://travis-ci.org/develrulez/thin-jar-launcher)
[![codecov.io](https://codecov.io/gh/develrulez/thin-jar-launcher/branch/master/graph/badge.svg)](https://codecov.io/gh/develrulez/thin-jar-launcher)
[![maven-central](https://maven-badges.herokuapp.com/maven-central/org.develrulez.thinjar/thin-jar-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.develrulez.thinjar/thin-jar-parent)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Maven Plugin

The Maven plugin aims to provide a lightweight solution, to build thin jars, which get its dependencies at launch time via a [proper locally installed Maven](https://maven.apache.org/install.html) distribution.

**[The Central Repository](http://central.sonatype.org/) integration is in process.**

### Thin Jar Goal

To build a thin jar, based on the original jar artifact, simply add the following plugin to your distributable jar artifact...

```xml
<plugin>
  <groupId>org.develrulez.thinjar</groupId>
  <artifactId>thin-jar-maven-plugin</artifactId>
  <version>a.b.c</version>
  <executions>
    <execution>
      <goals>
        <goal>thin-jar</goal>
      </goals>
      <configuration>
        <mainClass>${add_your_main_class_here}</mainClass>
      </configuration>
    </execution>
  </executions>
</plugin>
```

... and you'll get an additional artifact to your default jar with the suffix **-thin.jar**.

This thin jar artifact can basically be executed the traditional way:

```bash
java -jar *-thin.jar
```

Before the actual main class is executed, the launcher sets up a **lib** directory in the jars base directory, where the required application runtime dependencies are getting stored.

### Thin Linux Executable Goal

Based on the Coderwall article ['How to make a JAR file Linux executable'](https://coderwall.com/p/ssuaxa/how-to-make-a-jar-file-linux-executable), this goal uses the ability to append a generic binary payload to a Linux shell script.

```xml
<!-- Ommited plugin configuration and mandatory previous thin jar goal execution -->
<execution>
  <id>thin-linux-executable</id>
  <goals>
    <goal>thin-linux-executable</goal>
  </goals>
  <configuration>
    <!-- 
        Optional: Maven artifact, which contains the launch script to use. 
        A string of the form groupId:artifactId:version[:packaging[:classifier]] 
        -->
    <artifact>org.springframework.boot:spring-boot-loader-tools:${spring-boot.version}</artifact>
    <!-- 
        Optional: Relative or absolute launch script path.
        -->
    <script>org/springframework/boot/loader/tools/launch.script</script>
  </configuration>
</execution>
```

* Resulting artifact: target/*-thin.run
* Configuration management: See [documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html#deployment-script-customization-when-it-runs)

The above execution configuration example demonstrates the usage of a [launch script](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html#deployment-install-supported-operating-systems) which is used by the spring-boot-maven-plugin, to produce executable jars. 

### Thin Windows Executable Goal

... via [launch4j](http://launch4j.sourceforge.net).

```xml
<!-- Ommited plugin configuration and mandatory previous thin jar goal execution -->
<execution>
  <id>thin-windows-executable</id>
  <goals>
    <goal>thin-windows-executable</goal>
  </goals>
</execution>
```

* Resulting artifact: target/*-thin.exe
* Configuration management: See [documentation](http://launch4j.sourceforge.net/docs.html#Additional_jvm_options)

## Known 'inconsistencies'

### Spring Boot Developer Tools

When the artifact [spring-boot-devtools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html) is a part of your project dependencies without any further configuration, following error may occur after launching the thin jar:

```text
2017-10-20 18:35:09.438  INFO 4088 --- [  restartedMain] com.example.ExampleApplication           : Started ExampleApplication in 1.931 seconds (JVM running for 8.351)
Exception in thread "main" java.lang.IllegalStateException: Unable to execute starter class 'com.example.ExampleApplication'.
        at org.develrulez.thinjar.Launcher.launch(Launcher.java:41)
        at org.develrulez.thinjar.Launcher.main(Launcher.java:25)
Caused by: java.lang.reflect.InvocationTargetException
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.develrulez.thinjar.Launcher.launch(Launcher.java:39)
        ... 1 more
Caused by: org.springframework.boot.devtools.restart.SilentExitExceptionHandler$SilentExitException
        at org.springframework.boot.devtools.restart.SilentExitExceptionHandler.exitCurrentThread(SilentExitExceptionHandler.java:90)
        at org.springframework.boot.devtools.restart.Restarter.immediateRestart(Restarter.java:184)
        at org.springframework.boot.devtools.restart.Restarter.initialize(Restarter.java:163)
        at org.springframework.boot.devtools.restart.Restarter.initialize(Restarter.java:552)
        at org.springframework.boot.devtools.restart.RestartApplicationListener.onApplicationStartingEvent(RestartApplicationListener.java:67)
        at org.springframework.boot.devtools.restart.RestartApplicationListener.onApplicationEvent(RestartApplicationListener.java:45)
        at org.springframework.context.event.SimpleApplicationEventMulticaster.invokeListener(SimpleApplicationEventMulticaster.java:167)
        at org.springframework.context.event.SimpleApplicationEventMulticaster.multicastEvent(SimpleApplicationEventMulticaster.java:139)
        at org.springframework.context.event.SimpleApplicationEventMulticaster.multicastEvent(SimpleApplicationEventMulticaster.java:122)
        at org.springframework.boot.context.event.EventPublishingRunListener.starting(EventPublishingRunListener.java:69)
        at org.springframework.boot.SpringApplicationRunListeners.starting(SpringApplicationRunListeners.java:48)
        at org.springframework.boot.SpringApplication.run(SpringApplication.java:292)
        at org.springframework.boot.SpringApplication.run(SpringApplication.java:1118)
        at org.springframework.boot.SpringApplication.run(SpringApplication.java:1107)
        at com.example.ExampleApplication.main(ExampleApplication.java:10)
        ... 6 more
```

In this case you can [disable the restart feature of Spring Boot's developer tools](https://docs.spring.io/spring-boot/docs/current/reference/html/using-boot-devtools.html#using-boot-devtools-restart-disable) by combining it with a specialized property check:

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        // Don't fear a NullPointerException, because this check is null-resistent.
        if(Boolean.valueOf(System.getProperty("thinjar.launcher.active"))){
            System.setProperty("spring.devtools.restart.enabled", "false");
        }
        SpringApplication.run(ExampleApplication.class, args);
    }
}

```

... or you have to look out for another solution. Suggestions will be accepted with a special thanks :wink:.