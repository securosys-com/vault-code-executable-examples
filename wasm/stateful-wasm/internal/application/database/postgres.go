// SPDX-License-Identifier: Apache-2.0

package database

import (
	"context"
	"database/sql"
	"fmt"
	"net/url"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/stdlib"
	"securosys/tee-docker/examples/database-wasm/internal/application/config"
	"securosys/tee-docker/examples/database-wasm/internal/application/jdbc"
	"securosys/tee-docker/examples/database-wasm/internal/application/wasisocket"
)

type postgresAdapter struct {
	readDB, writeDB       *sql.DB
	readTable, writeTable string
}

func openPostgres(databaseConfig config.DatabaseConfig) (*sql.DB, string, error) {
	parsedURL, err := jdbc.ParseURL(databaseConfig.URL)
	if err != nil {
		return nil, "", err
	}
	params, err := parsedURL.PostgresParams()
	if err != nil {
		return nil, "", fmt.Errorf("process PostgreSQL JDBC options: %w", err)
	}
	table, err := jdbc.QuoteTable(databaseConfig.Table, jdbc.DialectPostgres)
	if err != nil {
		return nil, "", err
	}
	u := &url.URL{Scheme: "postgres", Host: parsedURL.Address(), Path: "/" + parsedURL.Database, RawQuery: params.Encode()}
	u.User = url.UserPassword(databaseConfig.Username, databaseConfig.Password)
	pgConfig, err := pgx.ParseConfig(u.String())
	if err != nil {
		return nil, "", fmt.Errorf("create PostgreSQL config: %w", err)
	}
	pgConfig.DialFunc = wasisocket.DialContext
	pgConfig.LookupFunc = wasisocket.LookupHost
	db := stdlib.OpenDB(*pgConfig)
	db.SetMaxOpenConns(1)
	return db, table, nil
}

func newPostgresAdapter(connection config.DatabaseConfig, master *config.DatabaseConfig) (*postgresAdapter, error) {
	readDB, readTable, err := openPostgres(connection)
	if err != nil {
		return nil, err
	}
	adapter := &postgresAdapter{readDB: readDB, writeDB: readDB, readTable: readTable, writeTable: readTable}
	if master != nil {
		adapter.writeDB, adapter.writeTable, err = openPostgres(*master)
		if err != nil {
			readDB.Close()
			return nil, fmt.Errorf("master database: %w", err)
		}
	}
	return adapter, nil
}

func (a *postgresAdapter) SaveState(key, value string) error {
	query := `INSERT INTO ` + a.writeTable + ` ("key", "value") VALUES ($1, $2) ON CONFLICT ("key") DO UPDATE SET "value" = EXCLUDED."value"`
	_, err := a.writeDB.ExecContext(context.Background(), query, key, value)
	return err
}

func (a *postgresAdapter) ReadState(key, defaultValue string) (string, error) {
	var value string
	err := a.readDB.QueryRowContext(context.Background(), `SELECT "value" FROM `+a.readTable+` WHERE "key" = $1`, key).Scan(&value)
	if err == sql.ErrNoRows {
		return defaultValue, nil
	}
	return value, err
}

func (a *postgresAdapter) DeleteState(key string) error {
	_, err := a.writeDB.ExecContext(context.Background(), `DELETE FROM `+a.writeTable+` WHERE "key" = $1`, key)
	return err
}

func (a *postgresAdapter) Close() error {
	if a.writeDB != a.readDB {
		_ = a.writeDB.Close()
	}
	return a.readDB.Close()
}
