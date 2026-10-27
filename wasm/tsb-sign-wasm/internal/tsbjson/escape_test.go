package tsbjson

import (
	"encoding/json"
	"testing"
)

func TestEscapeString(t *testing.T) {
	want := "quote \" and newline\nand tab\t"
	escaped := EscapeString(want)

	var got string
	if err := json.Unmarshal([]byte("\""+escaped+"\""), &got); err != nil {
		t.Fatalf("escaped string is not valid JSON: %v", err)
	}
	if got != want {
		t.Fatalf("escaped string decoded to %q, want %q", got, want)
	}
}
