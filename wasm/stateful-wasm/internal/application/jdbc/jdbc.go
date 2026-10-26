// SPDX-License-Identifier: Apache-2.0

package jdbc

import (
	"fmt"
	"net"
	"net/url"
	"regexp"
	"strconv"
	"strings"
)

type Dialect string

const (
	DialectPostgres Dialect = "postgresql"
	DialectMySQL    Dialect = "mysql"
)

type JDBCURL struct {
	Dialect  Dialect
	Host     string
	Port     string
	Database string
	Params   url.Values
}

var identifier = regexp.MustCompile(`^[A-Za-z_][A-Za-z0-9_$]*(\.[A-Za-z_][A-Za-z0-9_$]*)*$`)

func ParseURL(raw string) (JDBCURL, error) {
	var dialect Dialect
	var defaultPort string
	var rest string
	switch {
	case strings.HasPrefix(raw, "jdbc:postgresql://"):
		dialect, defaultPort = DialectPostgres, "5432"
		rest = strings.TrimPrefix(raw, "jdbc:postgresql://")
	case strings.HasPrefix(raw, "jdbc:mysql://"):
		dialect, defaultPort = DialectMySQL, "3306"
		rest = strings.TrimPrefix(raw, "jdbc:mysql://")
	case strings.HasPrefix(raw, "jdbc:mariadb://"):
		dialect, defaultPort = DialectMySQL, "3306"
		rest = strings.TrimPrefix(raw, "jdbc:mariadb://")
	default:
		return JDBCURL{}, fmt.Errorf("unsupported JDBC URL prefix")
	}

	u, err := url.Parse("db://" + rest)
	if err != nil || u.Hostname() == "" {
		return JDBCURL{}, fmt.Errorf("invalid JDBC URL %q", raw)
	}
	database, err := url.PathUnescape(strings.TrimPrefix(u.EscapedPath(), "/"))
	if err != nil || database == "" || strings.Contains(database, "/") {
		return JDBCURL{}, fmt.Errorf("JDBC URL must contain one database name")
	}
	port := u.Port()
	if port == "" {
		port = defaultPort
	}
	return JDBCURL{Dialect: dialect, Host: u.Hostname(), Port: port, Database: database, Params: u.Query()}, nil
}

func (u JDBCURL) Address() string { return net.JoinHostPort(u.Host, u.Port) }

func (u JDBCURL) PostgresParams() (url.Values, error) {
	params := u.Params.Clone()
	if useSSLValues, exists := params["useSSL"]; exists {
		if len(useSSLValues) != 1 {
			return nil, fmt.Errorf("PostgreSQL JDBC URL must have at most one useSSL value")
		}
		useSSL, err := strconv.ParseBool(useSSLValues[0])
		if err != nil {
			return nil, fmt.Errorf("invalid PostgreSQL JDBC useSSL value %q", useSSLValues[0])
		}
		if !params.Has("sslmode") {
			if useSSL {
				params.Set("sslmode", "require")
			} else {
				params.Set("sslmode", "disable")
			}
		}
		params.Del("useSSL")
	}
	params.Del("allowPublicKeyRetrieval")
	return params, nil
}

func QuoteTable(table string, dialect Dialect) (string, error) {
	if !identifier.MatchString(table) {
		return "", fmt.Errorf("invalid table identifier %q", table)
	}
	quote := `"`
	if dialect == DialectMySQL {
		quote = "`"
	}
	parts := strings.Split(table, ".")
	for i := range parts {
		parts[i] = quote + parts[i] + quote
	}
	return strings.Join(parts, "."), nil
}
