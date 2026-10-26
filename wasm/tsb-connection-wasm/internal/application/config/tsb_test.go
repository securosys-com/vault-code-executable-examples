// SPDX-FileCopyrightText: Copyright 2026 Securosys SA
// SPDX-License-Identifier: Apache-2.0

package config

import "testing"

func TestToAuthStructMapsBearerAndAPIKeys(t *testing.T) {
	connection := &TsbConnection{
		AppName:     "vaultcode",
		Auth:        "token",
		BearerToken: "bearer-token",
		APIKeys: &TsbAPIKeys{
			KeyOperationToken: []string{"operation-key"},
		},
	}

	auth, err := connection.ToAuthStruct()
	if err != nil {
		t.Fatalf("ToAuthStruct: %v", err)
	}
	if auth.AuthType != "TOKEN" || auth.BearerToken != "bearer-token" {
		t.Fatalf("unexpected bearer auth mapping: %#v", auth)
	}
	if len(auth.ApiKeys.KeyOperationToken) != 1 || auth.ApiKeys.KeyOperationToken[0] != "operation-key" {
		t.Fatalf("unexpected API key mapping: %#v", auth.ApiKeys)
	}
}

func TestToAuthStructMapsMutualTLSPEM(t *testing.T) {
	connection := &TsbConnection{Auth: "CERT", CertPEM: "certificate", KeyPEM: "private-key"}

	auth, err := connection.ToAuthStruct()
	if err != nil {
		t.Fatalf("ToAuthStruct: %v", err)
	}
	if auth.AuthType != "CERT" || auth.CertPEM != "certificate" || auth.KeyPEM != "private-key" {
		t.Fatalf("unexpected certificate auth mapping: %#v", auth)
	}
}

func TestToAuthStructRejectsIncompleteAuth(t *testing.T) {
	tests := []*TsbConnection{
		{Auth: "TOKEN"},
		{Auth: "CERT", CertPEM: "certificate"},
		{Auth: "CERT", CertPEM: "certificate", KeyPEM: "private-key", CertPath: "/client.crt"},
		{Auth: "UNKNOWN"},
	}
	for _, test := range tests {
		if _, err := test.ToAuthStruct(); err == nil {
			t.Errorf("ToAuthStruct(%#v) unexpectedly succeeded", test)
		}
	}
}