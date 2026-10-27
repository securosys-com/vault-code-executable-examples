GO ?= go
WASMTIME ?= wasmtime
GOCACHE ?= /tmp/database-wasm-gocache
GOMODCACHE ?= /tmp/database-wasm-gomodcache
WORLD := securosys:database/command@0.1.0
ARTIFACT := database-wasm.wasm
INPUT ?= input.json

.PHONY: all deps bindings test build run clean

all: test build

deps:
	GOCACHE=$(GOCACHE) GOMODCACHE=$(GOMODCACHE) $(GO) mod tidy

bindings:
	GOCACHE=$(GOCACHE) GOMODCACHE=$(GOMODCACHE) $(GO) tool componentize-go \
		--ignore-toml-files -d ./wit -w $(WORLD) bindings \
		-o bindings \
		--pkg-name securosys/tee-docker/examples/database-wasm/bindings \
		--include-versions --format

test:
	GOCACHE=$(GOCACHE) GOMODCACHE=$(GOMODCACHE) $(GO) test ./internal/...

build:
	GOCACHE=$(GOCACHE) GOMODCACHE=$(GOMODCACHE) $(GO) tool componentize-go \
		--ignore-toml-files -d ./wit -w $(WORLD) \
		build -o $(ARTIFACT)

run:
	$(WASMTIME) run -S cli=y -S inherit-network=y $(ARTIFACT) < $(INPUT)

clean:
	$(RM) $(ARTIFACT)
