# default-app WASM

Standalone Go implementation of the current Java `DefaultApplication` operation.
The existing JAR remains available. The service's `v1/upload_execution_code`
endpoint accepts this module alongside JAR uploads, recognizing core WASM
version 1 and WebAssembly Component Model headers. It skips JAR signer
verification for WASM, but keeps fingerprint restrictions and existing upload
authorization and size limits. Wasmtime validates the complete artifact before
execution.

Upload replaces the single active executable in the existing file/database
storage (the configured path may still end in `.jar`). Execution detects the
format from the sandbox copy: JAR uses Java, WASM uses Wasmtime 48.0.2, included
in the Docker image for AMD64 and ARM64. Both use the existing stdin/stdout,
timeout, output limits, result signatures and evidence flow.

The existing sandbox database user lifecycle and input fields, including shared
database and HSM configuration, are retained. The default WASM app ignores those
fields. No JDBC/JCE host functions are provided. Filesystem directories, host
environment variables and network access are not granted to this WASIp1 guest.
WASIp2 command components may use the `wasi:http` host interface when it is
explicitly enabled; raw socket inheritance remains disabled.

Optional service configuration:

```yaml
vaultcode:
  execution:
    maxMemorySize: 512MB
    wasm:
      runtimePath: /usr/local/bin/wasmtime
      toolsPath: /usr/local/bin/wasm-tools
      validationTimeout: 5s
      maxStackSize: 1MB
      engineTimeout: 5s
      maxTableElements: 100000
      maxWasiResources: 256
      hostcallFuel: 100000000
      maxRandomSize: 1MB
      memoryGuardSize: 64KB
      core:
        maxMemories: 1
        maxTables: 1
        maxInstances: 1
      component:
        maxMemories: 1
        maxTables: 2
        maxInstances: 4
      allowHttp: false
```

The memory limit applies to total executable-process RSS. WASM linear memory
receives 75% of it and the remaining 25% covers Wasmtime/JIT/native overhead.
The engine timeout is a second, independent interruption mechanism in addition
to the service watchdog. Compilation caching and environment inheritance are
disabled. Non-HTTP executions do not inherit network, DNS, TCP or UDP access.
WASI exposes stdin, stdout and stderr, but no environment variables or preopened
filesystem directories.
Optional WebAssembly proposals are disabled, and linear memory is pinned in a
fixed reservation protected by 64 KiB guard regions.

The module targets WASI Preview 1 (`wasip1/wasm`), supported by
[Go 1.21 and later](https://go.dev/blog/wasi). No third-party Go dependencies
are required.

## Build

From this directory:

```sh
GOOS=wasip1 GOARCH=wasm go build -trimpath -ldflags='-s -w' -o ../build/wasm/default-app.wasm .
```

The repository Gradle build publishes this example as
`executables/PositiveApprovalExecutable.wasm`.

## Input and output

Read one JSON envelope from stdin until EOF, using the existing `JvmInput` wire
format: `input` is a Base64-encoded UTF-8 task JSON. Within that task,
`approvalToBeSigned` is Base64-encoded output data.

For example, this envelope outputs exactly `Hello WASM` without a newline:

```json
{"input":"eyJhcHByb3ZhbFRvQmVTaWduZWQiOiJTR1ZzYkc4Z1YwRlRUUT09In0="}
```

Other envelope and task fields are ignored; database and HSM connections are
not used. Output is raw decoded bytes, not JSON. Invalid JSON, missing or empty
approval, and invalid Base64 cause a nonzero exit and a message on stderr.
This implementation covers the operation used by the Java application; it does
not reproduce Jackson validation of unrelated DTO fields.

## Run and verify

With Node.js 20+:

```sh
node run.mjs ../build/wasm/default-app.wasm < input.json > output.bin
python3 verify.py
```

Alternatively, with Wasmtime:

```sh
wasmtime run ../build/wasm/default-app.wasm < input.json > output.bin
```

To exercise the service execution path with a real runtime (mocking HSM and DB),
build the module first, then run from the repository root:

```sh
TEE_TEST_WASMTIME_PATH=/absolute/path/to/wasmtime \
TEE_TEST_WASM_TOOLS_PATH=/absolute/path/to/wasm-tools \
./gradlew :tee-service:test --tests com.securosys.tee.service.WasmExecutionTest
```

Uploads are validated fail-closed by the configured `wasm-tools` binary before
the active executable is replaced. Core modules may import only the supported
WASI Preview 1 functions and may not import memory or use shared memory.
Components must export `wasi:cli/run`; raw socket and unknown host interfaces
are rejected, and `wasi:http/types` plus `wasi:http/outgoing-handler` are
accepted only when `wasm.allowHttp` is enabled.

`run.mjs` is a local verification runner, not the service's production sandbox.
It grants no filesystem preopens and passes no host environment variables.
