package application

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"time"

	tsb "github.com/securosys-com/tsb-client-go"
	"go.bytecodealliance.org/pkg/wasihttp"
	"securosys/tee-docker/examples/tsb-sign-wasm/internal/application/config"
	"securosys/tee-docker/examples/tsb-sign-wasm/internal/tsbjson"
)

type signingRequest struct {
	KeyName string `json:"keyName"`
	Payload string `json:"payload"`
}

type signingResponse struct {
	Signature string `json:"signature"`
}

func Run() error {
	input, err := io.ReadAll(os.Stdin)
	if err != nil {
		return fmt.Errorf("read stdin: %w", err)
	}

	var envelope struct {
		Input         []byte                `json:"input"`
		TsbConnection *config.TsbConnection `json:"tsbConnection"`
	}
	if err := json.Unmarshal(input, &envelope); err != nil {
		return fmt.Errorf("invalid input envelope: %w", err)
	}
	if envelope.TsbConnection == nil {
		return fmt.Errorf("input does not contain tsbConnection")
	}

	var request signingRequest
	if err := json.Unmarshal(envelope.Input, &request); err != nil {
		return fmt.Errorf("invalid signing input: %w", err)
	}
	if strings.TrimSpace(request.KeyName) == "" {
		return fmt.Errorf("keyName is required")
	}

	auth, err := envelope.TsbConnection.ToAuthStruct()
	if err != nil {
		return fmt.Errorf("invalid TSB connection: %w", err)
	}
	client, err := tsb.NewTSBClientWithOptions(
		envelope.TsbConnection.RestAPI,
		auth,
		tsb.WithHTTPClient(&http.Client{
			Transport: wasihttp.DefaultTransport,
			Timeout:   30 * time.Second,
		}),
	)
	if err != nil {
		return fmt.Errorf("create TSB client: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	if err := ensureKey(ctx, client, request.KeyName); err != nil {
		return err
	}

	signature, _, err := client.Sign(
		ctx,
		tsbjson.EscapeString(request.KeyName),
		"",
		tsbjson.EscapeString(request.Payload),
		"UNSPECIFIED",
		tsb.SignatureAlgorithmSHA256WithRSA,
		tsb.SignatureTypeDER,
	)
	if err != nil {
		return fmt.Errorf("sign payload: %w", err)
	}
	if signature == nil || signature.Signature == "" {
		return fmt.Errorf("TSB returned an empty signature")
	}

	return json.NewEncoder(os.Stdout).Encode(signingResponse{Signature: signature.Signature})
}

func ensureKey(ctx context.Context, client *tsb.TSBClient, keyName string) error {
	keys, err := client.GetKeys(ctx)
	if err != nil {
		return fmt.Errorf("list TSB keys: %w", err)
	}
	for _, key := range keys {
		if key == keyName {
			return nil
		}
	}

	attributes := map[string]bool{
		"destroyable": true,
		"decrypt":     true,
		"encrypt":     true,
		"sign":        true,
		"verify":      true,
		"extractable": false,
		"sensitive":   false,
	}
	if _, err := client.CreateOrUpdateKey(ctx, tsbjson.EscapeString(keyName), "", attributes, "RSA", 2048, nil, "", false); err != nil {
		return fmt.Errorf("create RSA key %q: %w", keyName, err)
	}
	return nil
}
