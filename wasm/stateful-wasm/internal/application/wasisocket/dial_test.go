// SPDX-License-Identifier: Apache-2.0

package wasisocket

import (
	"reflect"
	"testing"
)

func TestLiteralAddresses(t *testing.T) {
	tests := []struct {
		host      string
		addresses []string
		wantMatch bool
	}{
		{host: "localhost", addresses: []string{"127.0.0.1", "::1"}, wantMatch: true},
		{host: "LOCALHOST.", addresses: []string{"127.0.0.1", "::1"}, wantMatch: true},
		{host: "127.0.0.1", addresses: []string{"127.0.0.1"}, wantMatch: true},
		{host: "::1", addresses: []string{"::1"}, wantMatch: true},
		{host: "db.internal", wantMatch: false},
	}

	for _, test := range tests {
		got, matched := literalAddresses(test.host)
		if matched != test.wantMatch {
			t.Errorf("literalAddresses(%q) matched = %t, want %t", test.host, matched, test.wantMatch)
			continue
		}
		if !matched {
			continue
		}
		if len(got) != len(test.addresses) {
			t.Errorf("literalAddresses(%q) returned %d addresses, want %d", test.host, len(got), len(test.addresses))
			continue
		}
		for i, address := range test.addresses {
			if got[i].String() != address {
				t.Errorf("literalAddresses(%q)[%d] = %q, want %q", test.host, i, got[i], address)
			}
		}
	}
}

func TestLookupHostResolvesLocalhostWithoutDNS(t *testing.T) {
	got, ok := lookupLiteralHost("localhost")
	if !ok {
		t.Fatal("localhost was not handled locally")
	}
	want := []string{"127.0.0.1", "::1"}
	if !reflect.DeepEqual(got, want) {
		t.Fatalf("lookupLiteralHost(localhost) = %v, want %v", got, want)
	}
}