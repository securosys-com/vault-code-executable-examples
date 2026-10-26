package config

import "testing"

func TestParseRequestDecodesJvmInputBytes(t *testing.T) {
	request, err := ParseRequest([]byte(`{
		"input":"Y291bnRlcg==",
		"database":{
			"url":"jdbc:postgresql://db.internal:5432/example",
			"username":"user",
			"password":"password",
			"table":"state"
		}
	}`))
	if err != nil {
		t.Fatal(err)
	}
	if got := string(request.Input); got != "counter" {
		t.Fatalf("decoded input = %q, want %q", got, "counter")
	}
}

func TestParseRequestRequiresDatabase(t *testing.T) {
	if _, err := ParseRequest([]byte(`{"input":"Y291bnRlcg=="}`)); err == nil {
		t.Fatal("request without database unexpectedly succeeded")
	}
}
