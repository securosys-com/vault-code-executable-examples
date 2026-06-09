# VaultCode Executable Examples

This repo contains several VaultCode executable examples. Additionally, we provide instructions on how to configure,
define your inputs and debugging.

## Table of Contents

- [Build apps](#build-apps)
- [Input](#input)
- [Output](#output)
- [Debug on IntelliJ IDEA](#debug-on-intellij-idea)

---

## Build apps

Building VaultCode requires the following minimum prerequisites

- Minimum Java version is **Java17**.

To build all jars with all mains use:

```shell
./gradlew clean buildAll
```

To build a single `.jar`, set the `MAIN` environment variable to the name of the class to build:

```shell
MAIN=PositiveApprovalExecutable ./gradlew clean build
```

If the `MAIN` environment variable is empty or not set, it defaults to `RunJarApplication`.

## Input

VaultCode reads input from stdin.

When VaultCode runs your app, the VaultCode runtime provides the input.
When developing locally, here are some ways by which you can pass data to your app:

```bash
java -jar example.jar < input.txt
# or
echo "data" | java -jar example.jar
# or
cat input.txt | java -jar example.jar
```

The VaultCode runtime provides data to your app in JSON format.
For example:

```json
{
  "input": "input in base64",
  "database": {
    "url": "jdbc url",
    "username": "temporary user",
    "password": "temporary password",
    "defaultTable": "name of default table"
  }
}
```

> [!note]
> The database properties are only present when `shareDatabase: true` is defined in the `application.yml`.

To read the input, do:

```java
String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
```

## Output

To generate output, your VaultCode app should write to stdout.
Make sure to flush the output buffer!

```java
System.out.write(output);
System.out.flush();
```

## Debug on IntelliJ IDEA

To allow debug on execution code, change the folloiwng Build and Test settings in IntelliJ IDEA
Go to **Settings** > **Build, Execution, Deployment** > **Build Tools** > **Gradle**
and change **"Build and run using"** and **"Run test using"** from Gradle to IntelliJ IDEA

Next modify your current run to provide input automatically.

- In **"Run/Debug Configurations"**, select current run or create new one that points to the expected main class.
- Next select **"Modify options"** in section **"Operating System"**  and check the option **"Redirect input"**.
- Now provide the file with json in field **"Redirect input from"**.
