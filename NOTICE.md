# Notice

This project includes the following dependencies:

## Maven Dependencies

### junit:junit:4.13.1
- Description: JUnit is a unit testing framework for Java.
- License: Eclipse Public License 1.0

### org.projectlombok:lombok:1.18.26
- Description: Lombok is a library that helps to reduce boilerplate code in Java classes.
- License: MIT License

### org.slf4j:slf4j-api:2.0.7
- Description: SLF4J is a simple facade for various logging frameworks.
- License: MIT License

### ch.qos.logback:logback-classic:1.4.7
- Description: Logback is a logging framework for Java applications.
- License: Eclipse Public License 1.0

### com.fasterxml.jackson.core:jackson-databind:2.15.0
- Description: Jackson is a JSON library for Java.
- License: Apache License 2.0

## Maven Plugins

### org.apache.maven.plugins:maven-compiler-plugin:3.11.0
- Description: The Maven Compiler Plugin provides support for compiling Java sources.
- License: Apache License 2.0

### org.apache.maven.plugins:maven-jar-plugin:3.3.0
- Description: The Maven JAR Plugin provides the capability to build and sign JARs.
- License: Apache License 2.0

### org.apache.maven.plugins:maven-shade-plugin:3.3.0
- Description: The Maven Shade Plugin provides the capability to create an Uber-JAR.
- License: Apache License 2.0

### com.github.spotbugs:spotbugs-maven-plugin:4.7.3.0
- Description: SpotBugs is a program that spots bugs in Java code.
- License: GNU Lesser General Public License (LGPL)

### org.apache.maven.plugins:maven-checkstyle-plugin:3.1.2
- Description: The Maven Checkstyle Plugin generates reports regarding the code style used by the developers.
- License: Apache License 2.0

### org.jacoco:jacoco-maven-plugin:0.8.8
- Description: JaCoCo is a Java Code Coverage library.
- License: Eclipse Distribution License 1.0

## Profiles

### check
- Description: This profile includes the Maven Checkstyle Plugin and SpotBugs Plugin for code analysis.
- License: Apache License 2.0, GNU Lesser General Public License (LGPL)

### coverage
- Description: This profile includes the JaCoCo Maven Plugin for code coverage reporting.
- License: Eclipse Distribution License 1.0
