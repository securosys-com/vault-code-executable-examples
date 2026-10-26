package config

import (
	"fmt"
	"strings"

	tsb "github.com/securosys-com/tsb-client-go"
)

type TsbConnection struct {
	RestAPI            string                 `json:"restApi"`
	AppName            string                 `json:"appName"`
	Auth               string                 `json:"auth"`
	BearerToken        string                 `json:"bearerToken"`
	CertPath           string                 `json:"certPath"`
	KeyPath            string                 `json:"keyPath"`
	CertPEM            string                 `json:"certPem"`
	KeyPEM             string                 `json:"keyPem"`
	APIKeys            *TsbAPIKeys            `json:"apiKeys"`
	ApplicationKeyPair *TsbApplicationKeyPair `json:"applicationKeyPair"`
}

type TsbAPIKeys struct {
	KeyManagementToken         []string `json:"keyManagementToken"`
	KeyOperationToken          []string `json:"keyOperationToken"`
	ApproverToken              []string `json:"approverToken"`
	ServiceToken               []string `json:"serviceToken"`
	ApproverKeyManagementToken []string `json:"approverKeyManagementToken"`
}

type TsbApplicationKeyPair struct {
	PrivateKey string `json:"privateKey"`
	PublicKey  string `json:"publicKey"`
}

func (connection *TsbConnection) ToAuthStruct() (tsb.AuthStruct, error) {
	if connection == nil {
		return tsb.AuthStruct{}, fmt.Errorf("connection is missing")
	}

	authType := strings.ToUpper(strings.TrimSpace(connection.Auth))
	if authType == "" {
		authType = "NONE"
	}

	auth := tsb.AuthStruct{AppName: connection.AppName, AuthType: authType}
	switch authType {
	case "NONE":
	case "TOKEN":
		if strings.TrimSpace(connection.BearerToken) == "" {
			return tsb.AuthStruct{}, fmt.Errorf("TOKEN auth requires bearerToken")
		}
		auth.BearerToken = connection.BearerToken
	case "CERT":
		if err := connection.copyCertificate(&auth); err != nil {
			return tsb.AuthStruct{}, err
		}
	default:
		return tsb.AuthStruct{}, fmt.Errorf("unsupported auth mode %q", authType)
	}

	if connection.APIKeys != nil {
		auth.ApiKeys = tsb.ApiKeyTypes{
			KeyManagementToken:         connection.APIKeys.KeyManagementToken,
			KeyOperationToken:          connection.APIKeys.KeyOperationToken,
			ApproverToken:              connection.APIKeys.ApproverToken,
			ServiceToken:               connection.APIKeys.ServiceToken,
			ApproverKeyManagementToken: connection.APIKeys.ApproverKeyManagementToken,
		}
	}
	if connection.ApplicationKeyPair != nil {
		privateKey := connection.ApplicationKeyPair.PrivateKey
		publicKey := connection.ApplicationKeyPair.PublicKey
		if (privateKey == "") != (publicKey == "") {
			return tsb.AuthStruct{}, fmt.Errorf("application key pair requires both private and public keys")
		}
		if privateKey != "" {
			auth.ApplicationKeyPair = tsb.KeyPair{PrivateKey: &privateKey, PublicKey: &publicKey}
		}
	}
	return auth, nil
}

func (connection *TsbConnection) copyCertificate(auth *tsb.AuthStruct) error {
	hasCertPEM := connection.CertPEM != ""
	hasKeyPEM := connection.KeyPEM != ""
	hasCertPath := connection.CertPath != ""
	hasKeyPath := connection.KeyPath != ""
	hasPEM := hasCertPEM && hasKeyPEM
	hasPaths := hasCertPath && hasKeyPath
	if hasCertPEM != hasKeyPEM || hasCertPath != hasKeyPath || hasPEM == hasPaths {
		return fmt.Errorf("CERT auth requires either certPem/keyPem or certPath/keyPath")
	}
	if hasPEM {
		auth.CertPEM = connection.CertPEM
		auth.KeyPEM = connection.KeyPEM
	} else {
		auth.CertPath = connection.CertPath
		auth.KeyPath = connection.KeyPath
	}
	return nil
}
