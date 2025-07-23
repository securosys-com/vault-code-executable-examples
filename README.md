
# Tee Docker

Java Springboot Application, that runs in sandbox mode jar files.

## Table of Contents

- [How to build and run](#how-to-build-and-run)
    - [Build apps](#build-docker-image)
    - [Input](#input)
    - [Output](#output)
    - [Test Run](#test-run)
    - [Debug on IntelliJ IDEA](#debug-on-intellij-idea)

---

## How to build and run
### Build apps
To build docker image for project, You must meet the following requirements:
- **Java** - min 17 version
To build all jars with all mains use:
```shell
 ./gradlew clean buildAll
```
It is possible to specify which *.jar You want to build
```shell
 MAIN=MAIN_CLASS_NAME ./gradlew clean build
```
example
```shell
 MAIN=PositiveApprovalApplication ./gradlew clean build
```
if env 'MAIN' is not provided, then ./gradlew will build
```shell
RunJarApplication.jar
```
### Input
Tee provides data to JVM as JSON.
Sample JSON Structure:
```json
{
  "input":"input in base64",
  "database": {
    "url": "jdbc url",
    "username": "temporary user",
    "password": "temporary password",
    "defaultTable": "name of default table"
  }
}
```
>*Note* - database properties are filled only, when **shareDatabase** is active on yml

In executable best way to read input from Tee is to use command:
```java
String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
```
in ```main()```
### Output
Return result to Tee is possible using these code:
```java
    System.out.write(returnInBytes);
    System.out.flush();
```
### Test Run
Tee do not use args to provide data to jar.
To run java code with data You have 2 options:
1) Pass input file to java ```java -jar example.jar < input.txt```
1) Using pipe ```echo "data" | java -jar example.jar``` or ```cat input.txt | java -jar example.jar``` 
It is done by that way, because send input data using arguments are limited and different by operating system.

### Debug on IntelliJ IDEA
To allow debug on execution code, 
Change settings in InteliJ: IDEA Build, Execution, Deployment > Build Tools > Gradle
and change **"Build and run using"** and **"Run test using"** from Gradle to IntelliJ IDEA

Next modify your current run to provide input automatically
In **"Run/Debug Configurations"**, select current run or create new one that point 
to expected main class. Next click **"Modify options"** in section **"Operating System"** check option
**"Redirect input"**. 
Now provide file with json in field **"Redirect input from"**.

