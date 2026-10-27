# WASM Executable Examples

This directory contains Go examples compiled to WebAssembly (WASM). WASM is a
portable executable format; WASI (the WebAssembly System Interface) provides
controlled access to host capabilities such as stdin/stdout, HTTP, and sockets.
The examples use the VaultCode JSON input envelope and write their results to
stdout, like the Java executable examples.

WASM does not automatically grant access to a filesystem, network, database, or
HSM. The host runtime must provide and authorize the capabilities that an
example needs.

## Build

Build WASM examples from the repository root with the Makefile:

```shell
make build-wasm
```

To build only one WASM example, use the corresponding target:

| Example | Command | Output |
| --- | --- | --- |
| Positive approval | `make build-wasm-positive-approval` | `executables/PositiveApprovalExecutable.wasm` |
| Stateful database | `make build-wasm-stateful` | `executables/StatefulExecutable.wasm` |
| TSB connection | `make build-wasm-tsb-connection` | `executables/TsbConnectionExecutable.wasm` |
| TSB signing | `make build-wasm-tsb-sign` | `executables/TsbSignExecutable.wasm` |
| TSB data object | `make build-wasm-tsb-dataobject` | `executables/TsbDataObjectExecutable.wasm` |

The build commands are defined in the repository-root `Makefile`; they use Go
and `componentize-go` directly, without invoking Gradle. Generated `.wasm`
files are written to `executables/`. To build both Java and WASM examples, run
`make build-all`.

## Examples

| Executable artifact | Example | What it does |
| --- | --- | --- |
| `PositiveApprovalExecutable.wasm` | Positive approval | Reads a VaultCode input envelope, decodes the task's Base64 `approvalToBeSigned` value, and writes those approval bytes to stdout. It does not call an external service or modify state. |
| `StatefulExecutable.wasm` | Stateful database | Treats the decoded input as a state key, reads its current integer value from PostgreSQL or MySQL/MariaDB (defaulting to `0`), increments and stores it, then prints the key and new value. |
| `TsbConnectionExecutable.wasm` | TSB connection | Uses the configured `tsbConnection` to call the TSB keystore statistics endpoint. It writes the request URL, HTTP status, and returned statistics as JSON. |
| `TsbSignExecutable.wasm` | TSB signing | Creates an RSA-2048 key in TSB if the requested key does not exist, signs the supplied payload with `SHA256_WITH_RSA`, and writes the returned signature as JSON. |
| `TsbDataObjectExecutable.wasm` | TSB data object | Performs the requested `save`, `read`, or `delete` operation on a TSB data object. Save stores the supplied value and attributes; read returns the value and attributes; delete removes the named object. |

All five read input from stdin and return results on stdout. The stateful and
TSB examples have external side effects: the stateful executable changes a
database value, while the TSB executables make service requests and the signing
and data-object executables can create, update, or delete TSB resources. Each
example is independent.

## Input required

When calling VaultCode, send only the `input` field shown below. Base64-encode
the value expected by the executable and place it in that field. VaultCode adds
the configured `database` or `tsbConnection` fields itself; do not include
those fields in your request.

### Positive approval

Before Base64-encoding, the `input` value is this task JSON. The
`approvalToBeSigned` property is itself Base64-encoded; this example returns
the bytes `Hello WASM`.

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

### Stateful database

Before Base64-encoding, the `input` value is the state key as plain text:
`counter`.

After Base64-encoding it, send this request to VaultCode:

```json
{
  "input": "Y291bnRlcg=="
}
```

### TSB connection

Before Base64-encoding, the `input` value is an empty JSON object:

```json
{}
```

After Base64-encoding that JSON, send this request to VaultCode:

```json
{
  "input": "e30="
}
```

### TSB signing

Before Base64-encoding, the `input` value is JSON with the key name and payload
to sign:

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

### TSB data object

For each action, start with the corresponding decoded JSON as the `input`
value. Base64-encode that JSON and send the resulting string in the outer
`input` field.

#### Save an object

Before Base64-encoding:

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

#### Read an object

Before Base64-encoding:

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

#### Delete an object

Before Base64-encoding:

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

## Configure VaultCode capabilities

Enable only the capabilities required by the selected example:

### TSB examples

The TSB connection, signing, and data-object components need both WASI HTTP
access and TSB connection details. Configure the TSB REST API and authentication
under `tsb.execution`, then enable VaultCode to pass that connection to
executables with `allowExecutableToConnectToTsb`. For example:

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

### Database example

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

### Positive approval example

The positive approval example only reads stdin and writes stdout. It needs no
network, database, or HTTP capability.
