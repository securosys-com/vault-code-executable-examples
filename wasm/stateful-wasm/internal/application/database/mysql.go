// SPDX-License-Identifier: Apache-2.0

package database

import (
	"context"
	"database/sql"
	"net"

	"github.com/go-sql-driver/mysql"
	"securosys/tee-docker/examples/database-wasm/internal/application/config"
	"securosys/tee-docker/examples/database-wasm/internal/application/jdbc"
	"securosys/tee-docker/examples/database-wasm/internal/application/wasisocket"
)

const wasiTCPNetwork = "wasi-tcp"

type mysqlAdapter struct {
	db    *sql.DB
	table string
}

func newMySQLAdapter(databaseConfig config.DatabaseConfig) (*mysqlAdapter, error) {
	parsedURL, err := jdbc.ParseURL(databaseConfig.URL)
	if err != nil {
		return nil, err
	}
	table, err := jdbc.QuoteTable(databaseConfig.Table, jdbc.DialectMySQL)
	if err != nil {
		return nil, err
	}
	mysql.RegisterDialContext(wasiTCPNetwork, func(ctx context.Context, address string) (netConn net.Conn, err error) {
		return wasisocket.DialContext(ctx, "tcp", address)
	})
	cfg := mysql.NewConfig()
	cfg.User, cfg.Passwd, cfg.Net, cfg.Addr, cfg.DBName = databaseConfig.Username, databaseConfig.Password, wasiTCPNetwork, parsedURL.Address(), parsedURL.Database
	cfg.Params = make(map[string]string, len(parsedURL.Params))
	for key, values := range parsedURL.Params {
		if len(values) != 0 {
			cfg.Params[key] = values[len(values)-1]
		}
	}
	connector, err := mysql.NewConnector(cfg)
	if err != nil {
		return nil, err
	}
	db := sql.OpenDB(connector)
	db.SetMaxOpenConns(1)
	return &mysqlAdapter{db: db, table: table}, nil
}

func (a *mysqlAdapter) SaveState(key, value string) error {
	query := "INSERT INTO " + a.table + " (`key`, `value`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)"
	_, err := a.db.ExecContext(context.Background(), query, key, value)
	return err
}

func (a *mysqlAdapter) ReadState(key, defaultValue string) (string, error) {
	var value string
	err := a.db.QueryRowContext(context.Background(), "SELECT `value` FROM "+a.table+" WHERE `key` = ? FOR UPDATE", key).Scan(&value)
	if err == sql.ErrNoRows {
		return defaultValue, nil
	}
	return value, err
}

func (a *mysqlAdapter) DeleteState(key string) error {
	_, err := a.db.ExecContext(context.Background(), "DELETE FROM "+a.table+" WHERE `key` = ?", key)
	return err
}

func (a *mysqlAdapter) Close() error { return a.db.Close() }
