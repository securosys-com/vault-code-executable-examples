# VaultCode Executable Examples

This repo contains several VaultCode executable examples. Additionally, we provide instructions on how to configure,
define your inputs and debugging.

## Table of Contents

- [Build apps](#build-apps)
- [Run executable](#run-executable)
- [Input](#input)
- [Input payload per main](#input-payload-per-main)
- [Output](#output)
- [Debug on IntelliJ IDEA](#debug-on-intellij-idea)

---

## Build apps

Building VaultCode requires the following minimum prerequisites

- Minimum Java version is **Java 17**.
- Access to Securosys JFrog Artifactory for the `com.securosys:primus-jce` dependency.

For local builds, you can use `gradle.properties` with properties:

```properties
com.securosys.artifactory_user=user-get-from-support-portal
com.securosys.artifactory_password=password-get-from-support-portal
```

To build all jars with all mains use:

```shell
./gradlew clean buildAll
```

To build a single `.jar`, set the `MAIN` environment variable to the name of the class to build:

```shell
MAIN=PositiveApprovalExecutable ./gradlew clean build
```

If the `MAIN` environment variable is empty or not set, it defaults to `RunJarExecutable`.

## Run executable

Each executable reads one JSON document from stdin and writes the result to stdout. The top-level `input` field must
be Base64 encoded. VaultCode provides this JSON at runtime; for local tests, create an input file and redirect it to
the jar.

Example input for the default `RunJarExecutable`:

```json
{
  "input": "aGVsbG8="
}
```

Run it locally:

```shell
java -jar build/libs/RunJarExecutable.jar < input.json
```

Result written to stdout:

```text
executed output of input: hello
```

Executables that work with approvals, HSM, or database access use the same top-level JSON envelope, but expect a
specific JSON payload inside the decoded `input` value and may also require `hsmConnection`, `database`,
`masterDatabase`, `counter`, or `timestamp`.

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
  "hsmConnection": {
    "host": "hsm url",
    "port": "hsm port",
    "user": "hsm user",
    "userSecret": "hsm user secret",
    "attestationKeyName": "attestation-key"
  },
  "database": {
    "url": "jdbc url",
    "username": "temporary user",
    "password": "temporary password",
    "table": "name of state table"
  }
}
```

> [!note]
> The database properties are only present when `shareDatabase: true` is defined in the `application.yml`.

The `hsmConnection` object follows the same structure as `test.json`. Use it when an executable needs to connect
to Primus HSM. It supports `host`, `port`, `user`, `userSecret`, `proxyUser`, `proxyPassword`, and
`attestationKeyName`.

To read the input, do:

```java
String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
```

## Input payload per main

The top-level `input` value is always Base64 encoded in the stdin JSON. The descriptions below show the decoded
value that each executable reads from `jvmInput.getInput()`.

| Main class | Decoded `jvmInput.getInput()` value | Additional top-level input |
| --- | --- | --- |
| `RunJarExecutable` | Any text or bytes. The example prints the decoded value after `executed output of input: `. | None |
| `ConnectionTestExecutable` | Any text. This class reads the top-level `input` directly, decodes it, prints `Hello <payload>`, then tests fixed hosts. | None |
| `StatefulExecutable` | A state key as plain text, for example `counter`. It reads the current value for this key, increments it, and saves it. | `database` is required. For PostgreSQL, `masterDatabase` may be used by the adapter. |
| `PositiveApprovalExecutable` | JSON matching `Task.TaskLevel6`. It requires `approvalToBeSigned` as Base64. | None |
| `RejectedApprovalExecutable` | JSON matching `Task.TaskLevel6`. It requires `approvalToBeSigned` as Base64. | None |
| `RuleBasedBtcTransactionSigningExecutable` | JSON matching `Task.TaskLevel6`. It requires `approvalToBeSigned` as Base64 and `metaData` as Base64 encoded JSON containing `Unsigned Transaction Hex`. | None |
| `FireblocksValidationExecutable` | JSON matching `Task.TaskLevel6`. It requires `approvalToBeSigned`, `metaData`, `metaDataSignature`, `payload`, and `signedSignRequest.signRequest`. | Fireblocks certificates and `SERVICE_NAME` constants must be configured in code before real validation. |
| `HsmExecutable` | JSON matching `CryptoOperation`, with `keyName` and `payload`. | `hsmConnection` is required. |

Minimal decoded payload for `PositiveApprovalExecutable` and `RejectedApprovalExecutable`:

```json
{
  "approvalToBeSigned": "dGVzdF9hcHByb3ZhbF90b19iZV9zaWduZWQ="
}
```

Minimal decoded payload for `StatefulExecutable`:

```text
counter
```

Minimal decoded payload for `HsmExecutable`:

```json
{
  "keyName": "RSA_SIGN_TEST",
  "payload": "dGVzdA=="
}
```

Minimal decoded payload for `RuleBasedBtcTransactionSigningExecutable`:

```json
{
  "approvalToBeSigned": "base64 challenge to return when accepted",
  "metaData": "base64 JSON with field: {\"Unsigned Transaction Hex\":\"...\"}"
}
```

Required decoded payload shape for `FireblocksValidationExecutable`:

```json
{
  "approvalToBeSigned": "base64 challenge to return when accepted",
  "metaData": "base64 Fireblocks metadata JSON",
  "metaDataSignature": "base64 signature of rawPayload",
  "payload": "payload that must match one messagesToSign[].message entry",
  "signedSignRequest": {
    "signRequest": {
      "signKeyName": "key id that must match metadata.signingDeviceKeyId",
      "signatureAlgorithm": "NONE_WITH_ECDSA or EDDSA"
    }
  }
}
```

Minimal stdin JSON examples:

`RunJarExecutable` and `ConnectionTestExecutable`:

```json
{
  "input": "dGVzdA=="
}
```

`PositiveApprovalExecutable` and `RejectedApprovalExecutable`:

```json
{
  "input": "eyJhcHByb3ZhbFRvQmVTaWduZWQiOiJkR1Z6ZEY5aGNIQnliM1poYkY5MGIxOWlaVjl6YVdkdVpXUT0ifQ=="
}
```

`StatefulExecutable`:

```json
{
  "input": "Y291bnRlcg==",
  "database": {
    "url": "jdbc:postgresql://localhost:5432/database_name",
    "username": "database_username",
    "password": "database_password",
    "table": "state"
  }
}
```

`HsmExecutable`:

```json
{
  "input": "eyJrZXlOYW1lIjoiUlNBX1NJR05fVEVTVCIsInBheWxvYWQiOiJkR1Z6ZEE9PSJ9",
  "hsmConnection": {
    "host": "hsm url",
    "port": "hsm port",
    "user": "hsm user",
    "userSecret": "hsm user secret",
    "attestationKeyName": "attestation-key"
  }
}
```

`RuleBasedBtcTransactionSigningExecutable`:

```json
{
  "input": "eyJhcHByb3ZhbFRvQmVTaWduZWQiOiJkR1Z6ZEY5aGNIQnliM1poYkY5MGIxOWlaVjl6YVdkdVpXUT0iLCJtZXRhRGF0YSI6ImV5SlZibk5wWjI1bFpDQlVjbUZ1YzJGamRHbHZiaUJJWlhnaU9pSXdNVEF3TURBd01EQXdNREV3TURBd01EQXdNREF3TURBd01EQXdNREF3TUNKOSJ9"
}
```

`FireblocksValidationExecutable`:

```json
{
  "input": "base64 encoded Fireblocks Task.TaskLevel6 JSON"
}
```

## Output

To generate output, your VaultCode app should write to stdout.
Make sure to flush the output buffer!

```java
System.out.write(output);
System.out.flush();
```

## Debug on IntelliJ IDEA

To allow debug on execution code, change the following Build and Test settings in IntelliJ IDEA
Go to **Settings** > **Build, Execution, Deployment** > **Build Tools** > **Gradle**
and change **"Build and run using"** and **"Run test using"** from Gradle to IntelliJ IDEA

Next modify your current run to provide input automatically.

- In **"Run/Debug Configurations"**, select current run or create new one that points to the expected main class.
- Next select **"Modify options"** in section **"Operating System"**  and check the option **"Redirect input"**.
- Now provide the file with json in field **"Redirect input from"**.
