# TSB data object WASM component

Uses `github.com/securosys-com/tsb-client-go` v1.4.0 for its data object API.

The component supports `save`, `read`, and `delete` actions. It expects the
VaultCode envelope used by `tsb-connection-wasm`: the inner request is JSON
encoded as Base64 in `input`, and `tsbConnection` contains TSB connection and
authentication settings.

## Input

Decoded `input` for saving a data object:

```json
{
  "action": "save",
  "name": "nazwa",
  "value": "plain text value",
  "attributes": {
    "modifiable": true,
    "destroyable": true,
    "private": true
  }
}
```

`value` is plain text in the input. The component Base64-encodes it for the TSB
API and decodes it again on `read`. `attributes` are passed through unchanged.
The `kmip` field is never populated.

For `read` and `delete`, only `action` and `name` are required. Data object
operations use the TSB key-management API key configured in
`tsbConnection.apiKeys.keyManagementToken`.

Successful `save` and `delete` responses include `saved: true` or `deleted:
true`. `read` returns `name`, the decoded `value`, and any stored `attributes`.

## Build

From the repository root:

```sh
./gradlew buildTsbDataObjectWasm
```

The resulting component is `executables/TsbDataObjectExecutable.wasm`.