# JAR Executable Examples

This directory contains Java examples compiled to JARs.

## Build

Building VaultCode requires the following minimum prerequisites

- Minimum Java version is **Java 17**.
- Access to Securosys JFrog Artifactory for the `com.securosys:primus-jce` dependency.

For local builds, credentials can be supplied through the environment:

```shell
export SECUROSYS_ARTIFACTORY_USER=user-get-from-support-portal
export SECUROSYS_ARTIFACTORY_PASSWORD=password-get-from-support-portal
```

Gradle automatically reads `java/gradle.properties` because it is located in
the Java project root. Local credentials can instead be stored in
`~/.gradle/gradle.properties` or supplied through the environment variables
above.

For IntelliJ IDEA, open `java/` as the Gradle project.

Build all Java JARs from the repository root with:

```shell
make build-java
```

To build one `.jar`, set `MAIN` to the class name:

```shell
cd java && MAIN=PositiveApprovalExecutable ./gradlew build
```

If the `MAIN` environment variable is empty or not set, it defaults to
`RunJarExecutable`.

## Examples

| Sample | Description |
| --- | --- |
| [Connection Test](src/main/java/com/securosys/tee/ConnectionTestExecutable.java) | Test connectivity to your application server |
| [Fireblocks Validation](src/main/java/com/securosys/tee/FireblocksValidationExecutable.java) | Validate a Fireblocks transaction request |
| [Execution Partition](src/main/java/com/securosys/tee/HsmExecutable.java) | Access an HSM partition via JCE. |
| [Positive Approval](src/main/java/com/securosys/tee/PositiveApprovalExecutable.java) | Accept all incoming SKA approval requests |
| [Rejected Approval](src/main/java/com/securosys/tee/RejectedApprovalExecutable.java) | Reject all incoming SKA approval requests |
| [Rule-based Bitcoin transactions Signing](src/main/java/com/securosys/tee/RuleBasedBtcTransactionSigningExecutable.java) | Rule-based Automated Crypto Wallet |
| [Hello World](src/main/java/com/securosys/tee/RunJarExecutable.java) | Simple echoing of the input |
| [Stateful with database](src/main/java/com/securosys/tee/StatefulExecutable.java) | Keep persistent arbitrary data between executions |

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

## Providing Input

When VaultCode runs your app, the VaultCode runtime provides the input.
When developing locally, here are some ways by which you can pass data to your app:

```bash
java -jar example.jar < input.txt
# or
echo "data" | java -jar example.jar
# or
cat input.txt | java -jar example.jar
```

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
| `HsmExecutable` | JSON matching `CryptoOperation`, with `keyName` and `payload`. | `hsmConnection` is required. Works with VaultCode version `2.2.0` or newer. |

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

Requires VaultCode version `2.2.0` or newer.

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
