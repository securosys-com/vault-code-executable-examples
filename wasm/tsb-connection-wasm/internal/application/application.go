// SPDX-FileCopyrightText: Copyright 2026 Securosys SA
// SPDX-License-Identifier: Apache-2.0

package application

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"time"

	tsb "github.com/securosys-com/tsb-client-go"
	"go.bytecodealliance.org/pkg/wasihttp"
	"securosys/tee-docker/examples/tsb-wasm/internal/application/config"
)

type connectionResult struct {
	URL        string          `json:"url"`
	StatusCode int             `json:"statusCode"`
	Statistics json.RawMessage `json:"statistics,omitempty"`
}

// Run reads the VaultCode JvmInput envelope from stdin, checks the TSB
// connection through wasi:http, and writes the result to stdout.
func Run() error {
	input, err := io.ReadAll(os.Stdin)
	if err != nil {
		return fmt.Errorf("read stdin: %w", err)
	}
	var envelope struct {
		Input         []byte               `json:"input"`
		TsbConnection *config.TsbConnection `json:"tsbConnection"`
	}
	if err := json.Unmarshal(input, &envelope); err != nil {
		return fmt.Errorf("invalid input envelope: %w", err)
	}
	if envelope.TsbConnection == nil {
		return fmt.Errorf("input does not contain tsbConnection")
	}
	auth, err := envelope.TsbConnection.ToAuthStruct()
	if err != nil {
		return fmt.Errorf("invalid TSB connection: %w", err)
	}

	httpClient := &http.Client{
		Transport: wasihttp.DefaultTransport,
		Timeout:   20 * time.Second,
	}
	client, err := tsb.NewTSBClientWithOptions(
		envelope.TsbConnection.RestAPI,
		auth,
		tsb.WithHTTPClient(httpClient),
	)
	if err != nil {
		return fmt.Errorf("create TSB client: %w", err)
	}

	body, status, err := client.CheckConnection(context.Background())
	if err != nil {
		return fmt.Errorf("TSB connection failed (HTTP %d): %w", status, err)
	}
	if !json.Valid([]byte(body)) {
		return fmt.Errorf("TSB returned invalid JSON (HTTP %d)", status)
	}

	return json.NewEncoder(os.Stdout).Encode(connectionResult{
		URL:        envelope.TsbConnection.RestAPI,
		StatusCode: status,
		Statistics: json.RawMessage(body),
	})
}
