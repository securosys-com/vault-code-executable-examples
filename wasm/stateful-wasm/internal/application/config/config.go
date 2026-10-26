// SPDX-License-Identifier: Apache-2.0

package config

import (
	"encoding/json"
	"fmt"
)

type DatabaseConfig struct {
	URL      string `json:"url"`
	Username string `json:"username"`
	Password string `json:"password"`
	Table    string `json:"table"`
}

type Request struct {
	Database       *DatabaseConfig `json:"database,omitempty"`
	MasterDatabase *DatabaseConfig `json:"masterDatabase,omitempty"`
	Input          []byte          `json:"input,omitempty"`
}

func ParseRequest(data []byte) (Request, error) {
	var request Request
	if err := json.Unmarshal(data, &request); err != nil {
		return Request{}, fmt.Errorf("invalid input JSON: %w", err)
	}

	if request.Database == nil {
		return Request{}, fmt.Errorf("input does not contain a database connection")
	}
	return request, nil
}
