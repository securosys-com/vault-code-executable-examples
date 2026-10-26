# VaultCode Executable Examples

This repo contains several VaultCode executable examples. Additionally, we provide instructions on how to configure,
define your inputs and debugging.

## Table of Contents

- [Build apps](#build-apps)
- [WebAssembly (WASM)](#webassembly-wasm)
- [Run executable](#run-executable)
- [Input](#input)
- [Input payload per main](#input-payload-per-main)
- [Output](#output)
- [Debug on IntelliJ IDEA](#debug-on-intellij-idea)

---

## Repository layout

- `java/` is the standalone Gradle project for the Java executable examples.
- `java/src/main/java/` contains the Java sources in Gradle's standard layout.
- `java/src/main/resources/` contains shared runtime resources.
- `wasm/` contains the Go and WIT sources for the WASM examples.
- `executables/` contains the built JAR and WASM artifacts.

## Build apps

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

## WebAssembly (WASM)

The examples in `wasm/` are Go programs compiled to portable WebAssembly
artifacts. WASI (the WebAssembly System Interface) provides controlled access
to host capabilities such as stdin/stdout, HTTP, and sockets. These examples
use the VaultCode JSON input envelope and write their results to stdout.

WASM does not automatically grant access to a filesystem, network, database, or
HSM. The host runtime must explicitly provide and authorize the capabilities
that an example needs.

Build a single WASM example with its Make target, or build all examples with
`make build-wasm`. Build both Java and WASM artifacts with `make build-all`.
The generated artifacts are written to `executables/`. The Java build is
managed by Gradle; WASM builds use Go and `componentize-go` commands defined in
the repository Makefile.

| WASM example | Command |
| --- | --- |
| Positive approval | `make build-wasm-positive-approval` |
| Stateful database | `make build-wasm-stateful` |
| TSB connection | `make build-wasm-tsb-connection` |
| TSB signing | `make build-wasm-tsb-sign` |
| TSB data object | `make build-wasm-tsb-dataobject` |

### WASM executables

| Executable artifact | What it does |
| --- | --- |
| `PositiveApprovalExecutable.wasm` | Decodes the task's Base64 `approvalToBeSigned` value and writes those approval bytes to stdout. It does not call an external service or modify state. |
| `StatefulExecutable.wasm` | Treats the decoded input as a state key, reads its current integer value from PostgreSQL or MySQL/MariaDB (defaulting to `0`), increments and stores it, then prints the key and new value. |
| `TsbConnectionExecutable.wasm` | Uses the configured `tsbConnection` to call the TSB keystore statistics endpoint. It writes the request URL, HTTP status, and returned statistics as JSON. |
| `TsbSignExecutable.wasm` | Creates an RSA-2048 key in TSB if the requested key does not exist, signs the supplied payload with `SHA256_WITH_RSA`, and writes the returned signature as JSON. |
| `TsbDataObjectExecutable.wasm` | Performs the requested `save`, `read`, or `delete` operation on a TSB data object. Save stores the supplied value and attributes; read returns the value and attributes; delete removes the named object. |

All five read input from stdin and return results on stdout. The stateful and
TSB examples have external side effects: the stateful executable changes a
database value, while the TSB executables make service requests and the signing
and data-object executables can create, update, or delete TSB resources.

### WASM input

When calling VaultCode, send only the `input` field shown below. Base64-encode
the value expected by the executable and place it in that field. VaultCode
adds the configured `database` or `tsbConnection` fields itself; do not include
those fields in your request.

#### Positive approval

Before Base64-encoding, the input is this task JSON. The `approvalToBeSigned`
property is itself Base64-encoded; this example returns the bytes `Hello WASM`.

```json
{
  "approvalToBeSigned": "SGVsbG8gV0FTTQ=="
}
```

After Base64-encoding that JSON, send this request to VaultCode:

```json
{
  "input": "eyJhcHByb3ZhbFRvQmVTaWduZWQiOiJTR1ZzYkc4Z1YwRlRUUT09In0="
}
```

#### Stateful database

Before Base64-encoding, the input is the state key as plain text: `counter`.

After Base64-encoding it, send this request to VaultCode:

```json
{
  "input": "Y291bnRlcg=="
}
```

#### TSB connection

Before Base64-encoding, the input is an empty JSON object:

```json
{}
```

After Base64-encoding that JSON, send this request to VaultCode:

```json
{
  "input": "e30="
}
```

#### TSB signing

Before Base64-encoding, the input is JSON with the key name and payload to sign:

```json
{
  "keyName": "demo-rsa",
  "payload": "text to sign"
}
```

After Base64-encoding that JSON, send this request to VaultCode:

```json
{
  "input": "eyJrZXlOYW1lIjoiZGVtby1yc2EiLCJwYXlsb2FkIjoidGV4dCB0byBzaWduIn0="
}
```

#### TSB data object

For each action, Base64-encode the decoded JSON and send the resulting string
in the outer `input` field.

Save an object, before Base64-encoding:

```json
{
  "action": "save",
  "name": "example-object",
  "value": "plain text value",
  "attributes": {
    "modifiable": true,
    "destroyable": true,
    "private": true
  }
}
```

After Base64-encoding:

```json
{
  "input": "eyJhY3Rpb24iOiJzYXZlIiwibmFtZSI6ImV4YW1wbGUtb2JqZWN0IiwidmFsdWUiOiJwbGFpbiB0ZXh0IHZhbHVlIiwiYXR0cmlidXRlcyI6eyJtb2RpZmlhYmxlIjp0cnVlLCJkZXN0cm95YWJsZSI6dHJ1ZSwicHJpdmF0ZSI6dHJ1ZX19"
}
```

Read an object, before Base64-encoding:

```json
{
  "action": "read",
  "name": "example-object"
}
```

After Base64-encoding:

```json
{
  "input": "eyJhY3Rpb24iOiJyZWFkIiwibmFtZSI6ImV4YW1wbGUtb2JqZWN0In0="
}
```

Delete an object, before Base64-encoding:

```json
{
  "action": "delete",
  "name": "example-object"
}
```

After Base64-encoding:

```json
{
  "input": "eyJhY3Rpb24iOiJkZWxldGUiLCJuYW1lIjoiZXhhbXBsZS1vYmplY3QifQ=="
}
```

### Configure VaultCode capabilities

Enable only the capabilities required by the selected example.

#### TSB examples

The TSB connection, signing, and data-object components need both WASI HTTP
access and TSB connection details. Configure the TSB REST API and
authentication under `tsb.execution`, then enable VaultCode to pass that
connection to executables with `allowExecutableToConnectToTsb`. For example:

```yaml
tsb:
  execution:
    rest-api: https://tsb.example.com
    app-name: vaultcode
    auth: TOKEN
    bearer-token: ${TSB_BEARER_TOKEN}
    api-keys:
      key-management-token:
        - ${TSB_KEY_MANAGEMENT_TOKEN}
      key-operation-token:
        - ${TSB_KEY_OPERATION_TOKEN}

vaultcode:
  allowExecutableToConnectToTsb: true
  execution:
    wasm:
      allowHttp: true
```

Use the authentication mode and API keys required by your TSB configuration.
`auth` supports `NONE`, `TOKEN`, or `CERT` in VaultCode's TSB configuration.
The connection component can use `NONE` or `TOKEN` directly over WASI HTTP;
WASI HTTP does not support direct client-certificate authentication. For TSB
deployments requiring mTLS, use an appropriately secured proxy that presents
the client certificate.

VaultCode forwards the configured connection as `tsbConnection` in the
executable's input envelope only when `allowExecutableToConnectToTsb` is true
and `tsb.execution.rest-api` is configured. The signing example needs key
management and key operation API keys; the data-object example needs a key
management API key. The connection-check example may not need API keys,
depending on the TSB endpoint's policy.

`allowHttp` enables the WASI HTTP interface only; it does not enable raw socket
access. Use secrets or your deployment's secret-management mechanism for
credentials rather than committing real tokens in configuration files.

#### Database example

The stateful example needs both VaultCode's stateful database feature and WASI
socket access. Enable both settings in the VaultCode service:

```yaml
vaultcode:
  statefulDatabaseExecutions: true
  execution:
    wasm:
      allowSockets: true
```

`statefulDatabaseExecutions` allows VaultCode to provide the executable's
`database` connection in the input envelope. The VaultCode service itself must
also be configured with its database connection and the required privileges
for managing executable state. `allowSockets` permits the WASIp2 component to
use TCP/IP to reach that database; this setting grants broad host network
access, not access limited to the configured database. Enable it only for
trusted, fingerprint-allowlisted executables, and restrict network egress at
the container or infrastructure layer where possible.

#### Positive approval example

The positive approval example only reads stdin and writes stdout. It needs no
network, database, or HTTP capability.

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
