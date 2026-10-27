// SPDX-License-Identifier: Apache-2.0

package application

import (
	"fmt"
	"io"
	"os"
	"strconv"

	"securosys/tee-docker/examples/database-wasm/internal/application/config"
	"securosys/tee-docker/examples/database-wasm/internal/application/database"
)

func Run() error {
	data, err := io.ReadAll(os.Stdin)
	if err != nil {
		return fmt.Errorf("read stdin: %w", err)
	}
	request, err := config.ParseRequest(data)
	if err != nil {
		return err
	}
	adapter, err := database.NewAdapter(*request.Database, request.MasterDatabase)
	if err != nil {
		return fmt.Errorf("create database adapter: %w", err)
	}
	defer adapter.Close()

	key := string(request.Input)
	value, err := adapter.ReadState(key, "0")
	if err != nil {
		return fmt.Errorf("read state for key %q: %w", key, err)
	}
	current, err := strconv.Atoi(value)
	if err != nil {
		return fmt.Errorf("state value for key %q is not an integer: %w", key, err)
	}
	next := current + 1
	if err := adapter.SaveState(key, strconv.Itoa(next)); err != nil {
		return fmt.Errorf("save state for key %q: %w", key, err)
	}
	if _, err := fmt.Fprintf(os.Stdout, "New value for key <%s> = %d", key, next); err != nil {
		return fmt.Errorf("write output: %w", err)
	}
	return nil
}
