package application

import (
	"context"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"time"

	tsb "github.com/securosys-com/tsb-client-go"
	"github.com/securosys-com/tsb-client-go/helpers"
	"go.bytecodealliance.org/pkg/wasihttp"
	"securosys/tee-docker/examples/tsb-dataobject-wasm/internal/application/config"
)

type dataObjectRequest struct {
	Action     string                        `json:"action"`
	Name       string                        `json:"name"`
	Value      *string                       `json:"value"`
	Attributes *helpers.DataObjectAttributes `json:"attributes"`
}

type dataObjectResponse struct {
	Action     string                        `json:"action"`
	Name       string                        `json:"name"`
	Value      *string                       `json:"value,omitempty"`
	Attributes *helpers.DataObjectAttributes `json:"attributes,omitempty"`
	Saved      bool                          `json:"saved,omitempty"`
	Deleted    bool                          `json:"deleted,omitempty"`
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

	var request dataObjectRequest
	if err := json.Unmarshal(envelope.Input, &request); err != nil {
		return fmt.Errorf("invalid data object input: %w", err)
	}
	request.Action = strings.ToLower(strings.TrimSpace(request.Action))
	if request.Action != "read" && request.Action != "save" && request.Action != "delete" {
		return fmt.Errorf("unsupported action %q: expected read, save, or delete", request.Action)
	}
	if strings.TrimSpace(request.Name) == "" {
		return fmt.Errorf("name is required")
	}
	if request.Action == "save" && request.Value == nil {
		return fmt.Errorf("value is required for save")
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

	response, err := execute(ctx, client, request)
	if err != nil {
		return err
	}
	return json.NewEncoder(os.Stdout).Encode(response)
}

func execute(ctx context.Context, client *tsb.TSBClient, request dataObjectRequest) (dataObjectResponse, error) {
	switch request.Action {
	case "save":
		object := helpers.DataObject{
			Name:       request.Name,
			Value:      base64.StdEncoding.EncodeToString([]byte(*request.Value)),
			Attributes: request.Attributes,
		}
		if _, err := client.CreateDataObject(ctx, object); err != nil {
			return dataObjectResponse{}, fmt.Errorf("save data object %q: %w", request.Name, err)
		}
		return dataObjectResponse{Action: request.Action, Name: request.Name, Saved: true}, nil
	case "read":
		object, _, err := client.GetDataObject(ctx, request.Name)
		if err != nil {
			return dataObjectResponse{}, fmt.Errorf("read data object %q: %w", request.Name, err)
		}
		if object == nil {
			return dataObjectResponse{}, fmt.Errorf("read data object %q: TSB returned no object", request.Name)
		}
		value, err := base64.StdEncoding.DecodeString(object.Value)
		if err != nil {
			return dataObjectResponse{}, fmt.Errorf("decode data object %q value: %w", request.Name, err)
		}
		decodedValue := string(value)
		return dataObjectResponse{
			Action:     request.Action,
			Name:       object.Name,
			Value:      &decodedValue,
			Attributes: object.Attributes,
		}, nil
	case "delete":
		if _, err := client.DeleteDataObject(ctx, request.Name); err != nil {
			return dataObjectResponse{}, fmt.Errorf("delete data object %q: %w", request.Name, err)
		}
		return dataObjectResponse{Action: request.Action, Name: request.Name, Deleted: true}, nil
	default:
		return dataObjectResponse{}, fmt.Errorf("unsupported action %q", request.Action)
	}
}