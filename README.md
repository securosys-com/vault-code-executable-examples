# VaultCode Executable Examples

This repo contains several VaultCode executable examples. Additionally, we provide instructions on how to configure,
define your inputs and debugging.

## Repository layout

- [`java/`](/java/README.md) is the standalone Gradle project for the **Java examples.**
- [`wasm/`](/wasm/README.md) contains the Go and WIT sources for the **WASM examples.**
- `executables/` contains pre-built JAR and WASM artifacts, ready to be uploaded to VaultCode.

## Documentation

See the [online documentation](https://docs.securosys.com/vaultcode/overview/)
for how to deploy and configure VaultCode (either on the HSM or an external Docker host for testing),
and how to upload and executes JAR/WASM files on VaultCode.

## Input

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

The `input` field contains the content received by VaultCode in the `executionRequest`
field of the [`POST /api/v1/execute` endpoint](https://docs.securosys.com/vaultcode/apispec/execute/).

The `database` properties are only present when `statefulDatabaseExecutions: true` is defined in the `application.yml`.
See the [administrator documentation](https://docs.securosys.com/vaultcode/Installation/Configuration/configure_database) for details..

The `hsmConnection` properties are present when the executable is allowed access to an "Execution Partition" on the HSM.
See the [administrator documentation](https://docs.securosys.com/vaultcode/Installation/Configuration/configure_execution_partition) for details..

## License

This repository is published under the terms of the [Apache 2.0](LICENSE) license.
