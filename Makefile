GRADLEW := ./java/gradlew
WASM_DIR := wasm
WASM_OUTPUT_DIR := executables
WASM_OUTPUT := $(abspath $(WASM_OUTPUT_DIR))
GO ?= go

.PHONY: build-all build-java build-wasm
.PHONY: build-wasm-positive-approval build-wasm-stateful
.PHONY: build-wasm-tsb-connection build-wasm-tsb-sign build-wasm-tsb-dataobject

build-all: build-java build-wasm

build-java:
	$(GRADLEW) -p java clean buildAll
	mkdir -p "$(WASM_OUTPUT_DIR)"
	cp java/build/libs/*.jar "$(WASM_OUTPUT_DIR)/"

build-wasm: build-wasm-positive-approval build-wasm-stateful \
	build-wasm-tsb-connection build-wasm-tsb-sign build-wasm-tsb-dataobject

build-wasm-positive-approval:
	mkdir -p "$(WASM_OUTPUT_DIR)"
	cd "$(WASM_DIR)/positive-approval-wasm" && GOOS=wasip1 GOARCH=wasm $(GO) build -trimpath -ldflags="-s -w" -o "$(WASM_OUTPUT)/PositiveApprovalExecutable.wasm" .

build-wasm-stateful:
	mkdir -p "$(WASM_OUTPUT_DIR)"
	cd "$(WASM_DIR)/stateful-wasm" && $(GO) tool componentize-go --ignore-toml-files -d ./wit -w securosys:database/command@0.1.0 build -o "$(WASM_OUTPUT)/StatefulExecutable.wasm"

build-wasm-tsb-connection:
	mkdir -p "$(WASM_OUTPUT_DIR)"
	cd "$(WASM_DIR)/tsb-connection-wasm" && $(GO) tool componentize-go --ignore-toml-files -d ./wit -w securosys:tsb/command@0.1.0 build -o "$(WASM_OUTPUT)/TsbConnectionExecutable.wasm"

build-wasm-tsb-sign:
	mkdir -p "$(WASM_OUTPUT_DIR)"
	cd "$(WASM_DIR)/tsb-sign-wasm" && $(GO) tool componentize-go --ignore-toml-files -d ../tsb-connection-wasm/wit -w securosys:tsb/command@0.1.0 bindings -o bindings --pkg-name securosys/tee-docker/examples/tsb-sign-wasm/bindings --include-versions --format
	cd "$(WASM_DIR)/tsb-sign-wasm" && $(GO) tool componentize-go --ignore-toml-files -d ../tsb-connection-wasm/wit -w securosys:tsb/command@0.1.0 build -o "$(WASM_OUTPUT)/TsbSignExecutable.wasm"

build-wasm-tsb-dataobject:
	mkdir -p "$(WASM_OUTPUT_DIR)"
	cd "$(WASM_DIR)/tsb-dataobject-wasm" && $(GO) tool componentize-go --ignore-toml-files -d ../tsb-connection-wasm/wit -w securosys:tsb/command@0.1.0 bindings -o bindings --pkg-name securosys/tee-docker/examples/tsb-dataobject-wasm/bindings --include-versions --format
	cd "$(WASM_DIR)/tsb-dataobject-wasm" && $(GO) tool componentize-go --ignore-toml-files -d ../tsb-connection-wasm/wit -w securosys:tsb/command@0.1.0 build -o "$(WASM_OUTPUT)/TsbDataObjectExecutable.wasm"
