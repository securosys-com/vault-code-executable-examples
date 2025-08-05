
# VaultCode Example apps

This repo contains several VaultCode example apps. Additionally, we provide instructions on how to configure,
define your inputs and debugging.

## Table of Contents

- [Build apps](#build-apps)
- [Input](#input)
- [Output](#output)
- [Test Run](#test-run)
- [Debug on IntelliJ IDEA](#debug-on-intellij-idea)

---

## Build apps

Building VaultCode requires the following minimum prerequisites

- Minimum Java version is **Java17**.

To build all jars with all mains use:

```shell
./gradlew clean buildAll
```

If you prefer, it is possible to specify which `.jar` you want to build by specifying the main_class_name

```shell
MAIN=MAIN_CLASS_NAME ./gradlew clean build

#example with PositiveApprovalExecutable

MAIN=PositiveApprovalExecutable ./gradlew clean build
```

if env 'MAIN' is not provided, then ./gradlew will build `RunJarApplication.jar`

## Input

VaultCode provides data to JVM as JSON. Sample JSON Structure:

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

>*Note* - database properties are filled only, when `shareDatabase:true` in the application.yml

For best experience, we recommend that when you rprovide input to VaultCode with your executable, to do so like this in `main()`:

```java
String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
```

## Output

In order for VaultCode to generate output, define:

```java
    System.out.write(returnInBytes);
    System.out.flush();
```

## Test Run

VaultCode does not need arguments to be provided to the jar.
To run java code with data, you have 2 options:

1) Using input file

```bash
java -jar example.jar < input.txt
```

2) Using pipe

```bash
echo "data" | java -jar example.jar

or

cat input.txt | java -jar example.jar
```

It is done in this way, as sending input via arguments is limited and can vary depending on OS.

## Debug on IntelliJ IDEA

To allow debug on execution code, change the folloiwng Build and Test settings in IntelliJ IDEA
Go to **Settings** > **Build, Execution, Deployment** > **Build Tools** > **Gradle**
and change **"Build and run using"** and **"Run test using"** from Gradle to IntelliJ IDEA

Next modify your current run to provide input automatically.

- In **"Run/Debug Configurations"**, select current run or create new one that points to the expected main class.
- Next select **"Modify options"** in section **"Operating System"**  and check the option **"Redirect input"**.
- Now provide the file with json in field **"Redirect input from"**.
