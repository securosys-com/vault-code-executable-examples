# TSB signing WASM component

This WASI Preview 2 component creates an RSA-2048 key when `keyName` is not
already present, then signs `payload` through `tsb-client-go` with
`SHA256_WITH_RSA`. Key creation uses the same attributes as the Java
`HsmExecutable`. The signature returned by TSB is written as JSON.

## Input

The component uses the VaultCode envelope from `tsb-connection-wasm`. The inner
`input` field is Base64-encoded JSON, and `tsbConnection` supplies the same TSB
authentication configuration used by the connection example:

```json
{
  "input": "eyJrZXlOYW1lIjoia2x1Y3pfcnNhIiwicGF5bG9hZCI6ImRhdGFfdG9fc2lnbiJ9",
  "tsbConnection": {
    "restApi": "https://tsb.example.com",
    "auth": "TOKEN",
    "bearerToken": "replace-me",
    "apiKeys": {
      "keyManagementToken": ["replace-me"],
      "keyOperationToken": ["replace-me"]
    }
  }
}
```

The decoded `input` is:

```json
{"keyName":"klucz_rsa","payload":"data_to_sign"}
```

Key creation requires a key-management API key. Signing requires a key-operation
API key. The output is:

```json
{"signature":"<signature returned by TSB>"}
```

## Build

From the repository root:

```sh
./gradlew buildTsbSignWasm
```

The resulting component is `executables/TsbSignExecutable.wasm`.