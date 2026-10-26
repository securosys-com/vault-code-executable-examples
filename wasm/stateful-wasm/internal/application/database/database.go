// SPDX-License-Identifier: Apache-2.0

package database

import (
	"fmt"

	"securosys/tee-docker/examples/database-wasm/internal/application/config"
	"securosys/tee-docker/examples/database-wasm/internal/application/jdbc"
)

type DatabaseAdapter interface {
	SaveState(key, value string) error
	ReadState(key, defaultValue string) (string, error)
	DeleteState(key string) error
	Close() error
}

func NewAdapter(connection config.DatabaseConfig, master *config.DatabaseConfig) (DatabaseAdapter, error) {
	parsed, err := jdbc.ParseURL(connection.URL)
	if err != nil {
		return nil, err
	}
	switch parsed.Dialect {
	case jdbc.DialectPostgres:
		return newPostgresAdapter(connection, master)
	case jdbc.DialectMySQL:
		if master != nil {
			return nil, fmt.Errorf("masterDatabase is only supported for PostgreSQL, matching the Java implementation")
		}
		return newMySQLAdapter(connection)
	default:
		return nil, fmt.Errorf("unsupported database dialect %q", parsed.Dialect)
	}
}
