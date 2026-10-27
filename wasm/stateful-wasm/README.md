# Database WASIp2 component

This example implements the Java `DatabaseAdapter` state operations in Go and
builds them as a WebAssembly Component Model command. Networking is provided by
`wasi:sockets@0.2.8` and `wasi:io@0.2.8`; it does not use WASI Preview 1
sockets.

## Project layout

```text
stateful-wasm/
├── main.go                 # component entry point only
├── Makefile                # build, test and run commands
├── bindings/               # generated Component Model bindings
├── internal/
│   └── application/        # stateful counter executable
│       ├── config/         # input envelope and database configuration
│       ├── database/       # adapter interface, PostgreSQL and MySQL
│       ├── jdbc/           # strict JDBC URL parsing and table quoting
│       └── wasisocket/     # net.Conn implemented with WASI 0.2 sockets
└── wit/                    # component world and pinned WASI 0.2.8 packages
```

Supported JDBC URL prefixes are `jdbc:postgresql://`, `jdbc:mysql://`, and
`jdbc:mariadb://`. The table must already exist with string-compatible `key`
and `value` columns and a unique/primary constraint on `key`.

## Input

The command mirrors the Java `StatefulExecutable`. It reads a VaultCode
`JvmInput`-compatible JSON envelope from stdin. Jackson represents Java
`byte[]` as base64, and Go decodes that field into `[]byte` in the same way:

```json
{
  "database": {
    "url": "jdbc:postgresql://db.internal:5432/example",
    "username": "temporary_user",
    "password": "temporary_password",
    "table": "state"
  },
  "input": "Y291bnRlcg=="
}
```

Here `Y291bnRlcg==` decodes to the key `counter`. The application reads the
current value with default `"0"`, parses it as an integer, increments it, saves
the new value, and writes:

```text
New value for key <counter> = 1
```

The WASI socket adapter resolves `localhost` directly to loopback addresses,
without querying DNS. When VaultCode and PostgreSQL run in separate containers,
use the PostgreSQL Compose service name in the JDBC URL instead of `localhost`.

For PostgreSQL, optional `masterDatabase` preserves the Java behavior: writes
and deletes use the master connection while reads use `database`. MariaDB/MySQL
uses only `database`.

## Build

Go 1.27.1 or newer is required:

```sh
cd wasm/stateful-wasm
make deps
make test
make build
```

The resulting customer-deliverable component is `database-wasm.wasm`.
The repository Gradle build publishes it as `executables/StatefulExecutable.wasm`.

The equivalent commands without `make` are:

```sh
GOCACHE=/tmp/database-wasm-gocache \
GOMODCACHE=/tmp/database-wasm-gomodcache \
go mod tidy

GOCACHE=/tmp/database-wasm-gocache \
GOMODCACHE=/tmp/database-wasm-gomodcache \
go tool componentize-go --ignore-toml-files \
  -d ./wit -w securosys:database/command@0.1.0 \
  bindings -o bindings \
  --pkg-name securosys/tee-docker/examples/database-wasm/bindings \
  --include-versions --format

GOCACHE=/tmp/database-wasm-gocache \
GOMODCACHE=/tmp/database-wasm-gomodcache \
go tool componentize-go --ignore-toml-files \
  -d ./wit -w securosys:database/command@0.1.0 \
  build -o database-wasm.wasm
```

The checked-in bindings make the separate `bindings` step necessary only when
the WIT world changes.

## Customer demo

1. Create the table using `demo/postgresql.sql` or `demo/mysql.sql`.
2. Copy `input.example.json` to `input.json` and enter temporary credentials.
3. Build and run the component twice:

```sh
make build
make run INPUT=input.json
make run INPUT=input.json
```

Expected output:

```text
New value for key <counter> = 1
New value for key <counter> = 2
```

For a customer hand-off, provide the source directory plus the compiled
`database-wasm.wasm`. Do not include a real `input.json` containing temporary
credentials; use `input.example.json` as the documented template.

## Run with Wasmtime

Raw network access must be explicitly inherited by the component:

```sh
wasmtime run -S cli=y -S inherit-network=y database-wasm.wasm < input.json
```

The embedding environment must permit the database host/port. WASI 0.2 does
not expose socket read/write deadlines, so the host should enforce the existing
execution timeout (Wasmtime epoch interruption is suitable).

TLS settings are passed through driver parameters. Supply the required
PostgreSQL `sslmode` or Go MySQL driver TLS configuration for your deployment.
For PostgreSQL, JDBC `useSSL` is translated to `sslmode=require` or
`sslmode=disable`; `allowPublicKeyRetrieval` is ignored because it is MySQL-only.
