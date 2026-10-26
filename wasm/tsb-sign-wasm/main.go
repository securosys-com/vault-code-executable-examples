package main

import (
	"fmt"
	"os"

	_ "securosys/tee-docker/examples/tsb-sign-wasm/bindings/wit_exports"
	"securosys/tee-docker/examples/tsb-sign-wasm/internal/application"
)

func main() {
	if err := application.Run(); err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}
