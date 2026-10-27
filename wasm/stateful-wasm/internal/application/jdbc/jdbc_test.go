package jdbc

import (
	"testing"
)

func TestParseJDBCURL(t *testing.T) {
	tests := []struct {
		url, dialect, address, database string
	}{
		{"jdbc:postgresql://db.internal/example", "postgresql", "db.internal:5432", "example"},
		{"jdbc:mysql://db.internal:3307/example?charset=utf8mb4", "mysql", "db.internal:3307", "example"},
		{"jdbc:mariadb://[2001:db8::1]/example", "mysql", "[2001:db8::1]:3306", "example"},
	}
	for _, test := range tests {
		got, err := ParseURL(test.url)
		if err != nil {
			t.Fatalf("parseJDBCURL(%q): %v", test.url, err)
		}
		if string(got.Dialect) != test.dialect || got.Address() != test.address || got.Database != test.database {
			t.Errorf("parseJDBCURL(%q) = %#v", test.url, got)
		}
	}
}

func TestParseJDBCURLRejectsLooseMatches(t *testing.T) {
	for _, raw := range []string{"postgresql://host/db", "jdbc:other://postgresql/db", "jdbc:h2:mem:test"} {
		if _, err := ParseURL(raw); err == nil {
			t.Errorf("parseJDBCURL(%q) unexpectedly succeeded", raw)
		}
	}
}

func TestPostgresParamsConvertsJDBCOptions(t *testing.T) {
	tests := []struct {
		name        string
		query       string
		wantSSLMode string
		wantError   bool
	}{
		{name: "disable TLS", query: "useSSL=false&allowPublicKeyRetrieval=true", wantSSLMode: "disable"},
		{name: "enable TLS", query: "useSSL=true", wantSSLMode: "require"},
		{name: "explicit SSL mode wins", query: "useSSL=false&sslmode=verify-full", wantSSLMode: "verify-full"},
		{name: "ignore MySQL-only option", query: "allowPublicKeyRetrieval=true"},
		{name: "reject invalid useSSL", query: "useSSL=maybe", wantError: true},
	}

	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			parsed, err := ParseURL("jdbc:postgresql://db.internal/example?" + test.query)
			if err != nil {
				t.Fatalf("ParseURL: %v", err)
			}
			got, err := parsed.PostgresParams()
			if test.wantError {
				if err == nil {
					t.Fatal("PostgresParams unexpectedly succeeded")
				}
				return
			}
			if err != nil {
				t.Fatalf("PostgresParams: %v", err)
			}
			if got.Has("useSSL") || got.Has("allowPublicKeyRetrieval") {
				t.Fatalf("JDBC-only parameters were not removed: %v", got)
			}
			if test.wantSSLMode != "" && got.Get("sslmode") != test.wantSSLMode {
				t.Errorf("sslmode = %q, want %q", got.Get("sslmode"), test.wantSSLMode)
			}
			if test.wantSSLMode == "" && got.Has("sslmode") {
				t.Errorf("unexpected sslmode %q", got.Get("sslmode"))
			}
		})
	}
}

func TestQuoteTable(t *testing.T) {
	if got, _ := QuoteTable("app.state", DialectPostgres); got != `"app"."state"` {
		t.Fatalf("PostgreSQL quoted table = %q", got)
	}
	if got, _ := QuoteTable("state", DialectMySQL); got != "`state`" {
		t.Fatalf("MySQL quoted table = %q", got)
	}
	if _, err := QuoteTable("state; DROP TABLE state", DialectPostgres); err == nil {
		t.Fatal("unsafe identifier accepted")
	}
}
