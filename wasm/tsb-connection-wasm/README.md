# TSB WASM component

This example builds a Go WebAssembly command component targeting WASI Preview 2. It
uses `github.com/securosys-com/tsb-client-go` v1.4.0 and injects
`wasihttp.DefaultTransport` through `WithHTTPClient`. The transport maps Go's
`net/http` calls to `wasi:http/outgoing-handler`.

One VaultCode stdin envelope triggers:

```text
GET https://engineering.securosys.com/tsb-demo/v1/keystore/statistics
```

TSB authentication is `NONE`.

## Quick start

Run these commands from the `tee-docker` repository root:

```sh
cd wasm/tsb-connection-wasm

GOCACHE=/private/tmp/tsb-wasm-gocache \
GOMODCACHE=/private/tmp/tsb-wasm-gomodcache \
go mod tidy

GOCACHE=/private/tmp/tsb-wasm-gocache \
GOMODCACHE=/private/tmp/tsb-wasm-gomodcache \
go tool componentize-go \
  --ignore-toml-files \
  -d ./wit \
  -w securosys:tsb/command@0.1.0 \
  build -o tsb-wasm.wasm
```

The generated artifact is:

```text
wasm/tsb-connection-wasm/tsb-wasm.wasm
```

The repository Gradle build publishes it as
`executables/TsbConnectionExecutable.wasm`.

## Requirements

- Go 1.27.1 or newer
- Wasmtime with Component Model and `wasi:http` support
- Internet access for the initial Go module download

`componentize-go` is declared as a Go tool dependency and does not need to be
installed globally.

## Build

```sh
GOCACHE=/private/tmp/tsb-wasm-gocache \
GOMODCACHE=/private/tmp/tsb-wasm-gomodcache \
go mod tidy

GOCACHE=/private/tmp/tsb-wasm-gocache \
GOMODCACHE=/private/tmp/tsb-wasm-gomodcache \
go tool componentize-go \
  --ignore-toml-files \
  -d ./wit \
  -w securosys:tsb/command@0.1.0 \
  build -o tsb-wasm.wasm
```

The output is a WASIp2 component, not a WASIp1 core module.

## Run directly with Wasmtime

```sh
wasmtime run \
  -S cli=y \
  -S http=y \
  tsb-wasm.wasm < input.json
```

Expected response shape:

```json
{
  "url": "https://engineering.securosys.com/tsb-demo",
  "statusCode": 200,
  "statistics": {
    "keyStoreStatistics": "...",
    "ApiLoginRole": "General"
  }
}
```

Raw socket inheritance is intentionally not enabled. Wasmtime implements the
outgoing request through the narrower `wasi:http` host interface.

## Authentication

The component supports these authentication modes directly over `wasi:http`:

| `auth` | Supported | Description |
| --- | --- | --- |
| `NONE` | Yes | No HTTP `Authorization` header is added. |
| `TOKEN` | Yes | Sends `Authorization: Bearer <bearerToken>`. |
| `CERT` | No | Direct client-certificate authentication (mTLS) is not available through the current `wasi:http` transport. |

API keys can be used with `NONE` or `TOKEN`; `tsb-client-go` sends the selected
key in the `X-API-KEY` header.

### mTLS through a proxy

For mTLS, point the component at a host-side proxy such as nginx, Envoy, or
HAProxy. The component connects to the proxy with `auth: NONE` or `auth: TOKEN`,
and the proxy presents the client certificate when it connects to TSB:

```text
tsb-wasm --wasi:http--> proxy --HTTPS + client certificate--> TSB REST API
```

Example nginx configuration:

```nginx
events {}

http {
    server {
        listen 8080;

        location / {
            proxy_pass https://tsb.example.com;
            proxy_set_header Host tsb.example.com;
            proxy_set_header Authorization $http_authorization;
            proxy_set_header X-API-KEY $http_x_api_key;

            proxy_ssl_server_name on;
            proxy_ssl_name tsb.example.com;
            proxy_ssl_certificate /etc/nginx/mtls/client.crt;
            proxy_ssl_certificate_key /etc/nginx/mtls/client.key;
            proxy_ssl_trusted_certificate /etc/nginx/mtls/tsb-ca.crt;
            proxy_ssl_verify on;
            proxy_ssl_verify_depth 3;
        }
    }
}
```

For example, if the proxy is reachable in the container network as
`tsb-mtls-proxy`, configure VaultCode as follows:

```yaml
tsb:
  execution:
    restApi: http://tsb-mtls-proxy:8080
    appName: vaultcode
    auth: TOKEN
    bearerToken: replace-me

vaultcode:
  allowExecutableToConnectToTsb: true
  execution:
    wasm:
      allowHttp: true
```

Use `auth: NONE` and omit `bearerToken` when TSB requires only the client
certificate. The certificate and private key belong to the proxy configuration,
not to `tsb-wasm`. The plain HTTP listener above is intended only for a trusted,
isolated container network; use TLS between VaultCode and the proxy when that
network is not trusted.

## Run through VaultCode

Enable HTTP for trusted WASIp2 components:

```yaml
vaultcode:
  execution:
    wasm:
      allowHttp: true
```

VaultCode's upload gate validates the component with the pinned `wasm-tools`,
requires the `wasi:cli/run` export, and rejects `wasi:http` imports unless this
setting is enabled. JAR, Core WASM and WASIp2 artifacts share the effective
`vaultcode.maxExecutableSize` upload limit.

Upload `tsb-wasm.wasm` through `v1/upload_execution_code`, then call the
normal VaultCode execution endpoint. VaultCode detects the Component Model
header, supplies `wasi:cli` and `wasi:http`, forwards its existing JSON envelope
on stdin, captures stdout, and applies the existing timeout, output limits,
fingerprint, signature, and evidence flow.
