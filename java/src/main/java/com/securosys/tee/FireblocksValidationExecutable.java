/**
 * Copyright (c)2025 Securosys SA, authors: Mikolaj Szargut
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 **/
package com.securosys.tee;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securosys.tee.dto.JvmInput;
import com.securosys.tee.dto.request.Task;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

public class FireblocksValidationExecutable {


    /** To verify payload signature in messages, this is vs - Vault Service certificate
     * example: MIIFXDCCA0SgAwIBAgIUTg3yFY4h+JmhBVyuJnw3znNwZFEwDQYJKoZIhvcNAQELBQAwfzELMAkGA1UEBhMCSUwxDzANBgNVBAgMBklzcmFlbDERMA8GA1UEBwwIVGVsIEF2aXYxJjAkBgNVBAoMHUZpcmVibG9ja3MgUHJvZHVjdGlvbiBSb290IENBMQ8wDQYDVQQLDAZEZXZPcHMxEzARBgNVBAMMCnRydXN0ZWRfY2EwHhcNMjMwODE1MTg1ODM0WhcNMjcwOTIzMTg1ODM0WjBwMQswCQYDVQQGEwJJTDEPMA0GA1UECAwGSXNyYWVsMScwJQYDVQQKDB5GaXJlYmxvY2tzIFByb2R1Y3Rpb24gU2VydmljZXMxDzANBgNVBAsMBkRldk9wczEWMBQGA1UEAwwNdmF1bHRfc2VydmljZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALwWXYXW75SeRL1dQZ4PEpqEz4D27fr4prx+yjWX9UX0yM3l1q7StnbBHCaWW+wFKc2kFE14WXZq7FQyB68Im7MK30apJ/Cj9JgjJDPy9wHDEo+XJq0CHQ9afmDwiDI6wNnyuHa7awB8kf2hDfA+O65I0ExyV5ogt5V2yZqYDhdsR48O14lNW6oT903RR6lUyYQA3ha82hpfQToydA08p/MPKCPiIRtMkhmQPznjB0nKBiV7dH3jeRiuRyqiiobwAWI0hZsVxagLjc1SeAgjhWQaHEqV0RMgCU3XlKcaezE6aZ2db3wVCrEbXQTWq6fucAOecTQSQgUuCiC3iRw2OK0CAwEAAaOB3jCB2zAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwHQYDVR0OBBYEFHfn5X5OkFcSwwjAvSHoJeFUo2NbMB8GA1UdIwQYMBaAFGo9sFPtBx9vLS4YC6XRBkfwbWjGMBEGCWCGSAGG+EIBAQQEAwIGwDAvBglghkgBhvhCAQ0EIhYgRmlyZWJsb2NrcyBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwGAYDVR0RBBEwD4INdmF1bHQtc2VydmljZTANBgkqhkiG9w0BAQsFAAOCAgEAPrhKd+UHXRfX+MBip7ggz72kgoZRvJ0HcTv1Och3gvUIjYqjk8A5T5/92+sLnUAZ6vSSPrqJX9qRN1UCPMrlP2+rB9TCy8M9dfLw/WJjlAJJ9cEvBX8hv+bcGMVA9uT6h3R8NiVriKJOngBCANEg08oTOULbyyQadNYq8S/gZUkWmth6TjW+kEM7BmPYAdiCqsQvNpKHXtAqOJRCmjMQ4Kp7/DMz4Gl8+qVwZezAkCqZ6PVtuKo8KX/PndZE5Ej7u0iY7zJcmJtKYHCgKXE84Qth1EmWj0udicTssaRGi68OzuHZI1FNP58dlDH8hiFWjrwybNMEwoBFY2ujMVYZdCKZBE2SXU+l6d010aNzXLQhVkFLXOxu+ZX97N3xP7mZiODEZcwcCShzx3Oc07kKwokybnJ5NEghLk0Osz2QIfsuZNKxenEJzrsbxbNKbzLPfJLDrrkmK9MXsfCBV9s9vjsb6dlYPwXOHUNQeJdaswF69oA+w03Kjv5bRq+DAf8Hn7bj3UPcmn4kXQi4M+ukJiU/YTPHNb0fSbYF4xb/gLZHBLxSvUCuaX3qirCJvn8b6Uncr2fm0PuC1eEHnKdYWIQLo3NRHVzNFezUvKE6/sXBONqG7Q3O5cs2W5yyb2MbKlflvKEqKxjCeZ0xujgNMLou10K7QKa8AhWm32yQzhM=
     */
     private static final String PAYLOAD_CERTIFICATE_BASE64 = "REPLACE_ME_BASE64_VS_CERTIFICATE";

    /** To verify txMetadata signature in messages, this is ps - Policy Service certificate
     * example: MIIFXjCCA0agAwIBAgIUTg3yFY4h+JmhBVyuJnw3znNwZFYwDQYJKoZIhvcNAQELBQAwfzELMAkGA1UEBhMCSUwxDzANBgNVBAgMBklzcmFlbDERMA8GA1UEBwwIVGVsIEF2aXYxJjAkBgNVBAoMHUZpcmVibG9ja3MgUHJvZHVjdGlvbiBSb290IENBMQ8wDQYDVQQLDAZEZXZPcHMxEzARBgNVBAMMCnRydXN0ZWRfY2EwHhcNMjMwODE1MTg1ODM0WhcNMjcwOTIzMTg1ODM0WjBxMQswCQYDVQQGEwJJTDEPMA0GA1UECAwGSXNyYWVsMScwJQYDVQQKDB5GaXJlYmxvY2tzIFByb2R1Y3Rpb24gU2VydmljZXMxDzANBgNVBAsMBkRldk9wczEXMBUGA1UEAwwOcG9saWN5X3NlcnZpY2UwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDAmWtwPRSxYYaojTqveaBTi9QZN8NI51U046j8jXLTaJ9QDRKN2WuiLHzsf0Y628n88kgJmFF5sP0zvxLe741yiixj83t2se52JrdKhSU04sCXuR17zkOzc4FB2TvLkdM79qQvzNBvFR6uFIZps3uAtujyVPhLBOz+E1UumasgxxyuMJlzv5zLPGJVA6HhIBDzn6joeYA00DujV1j4MTPcitNYWuOVoPNTm40+Ecp1mAgoAjHn5hbUnTZsiaILaOSttfWYA7thn4nutvh7MBq+MrYzY87hfLCAxm+QqohRk2lU86r8oPZpCtKg05W6NS+uTKFL9mOKBmlab7eKwLazAgMBAAGjgd8wgdwwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCBaAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMB0GA1UdDgQWBBRobyooq8YNwpdS+XvTcbazuRRUDTAfBgNVHSMEGDAWgBRqPbBT7Qcfby0uGAul0QZH8G1oxjARBglghkgBhvhCAQEEBAMCBsAwLwYJYIZIAYb4QgENBCIWIEZpcmVibG9ja3MgR2VuZXJhdGVkIENlcnRpZmljYXRlMBkGA1UdEQQSMBCCDnBvbGljeS1zZXJ2aWNlMA0GCSqGSIb3DQEBCwUAA4ICAQB9DDYg0ZXoLLMg+Q+9mIDLl+pK2yY/iSui+Y9+WR7Fya8NWUkgZDZ/vRJ/TE3JWAhjA7PcIAkvpp70GbcTbsl5Xf37pE1jkpnFYDNQfyLJLqPUhdNMvhqzI+80BbQZ9I3UEjS7+GqCFxKgLBaPVCrTGqA7Da966FZlZBadC3EgsVXs/VDZbktiAq8mwF5xGSzXOIT+XO/CrseQE/KbNMElfJq11CB5x6RHYyWZWLxOHuC35+4vdV4oAnyjiO13e0rT8ZsLc8DoyyxOzJTrek8Pdo1BpQ7pmE1ywUkp4gnL5v4uV/3VtEl6x8o9JEkUMjorNSlGo6Olnh9OnDmYoOzSKVm4hcz6FW2MbPISlFJW4DCWMyRK9ksnnBRwk8ew15TuYgcLou8rIrw0J5UF+ytVdoBmyFJdpeNIDf2JZYfRybXFxOERELvHCKZ2iYEMb70OrIKOElso8hZS33BT/VLjw7UcLfjek4h+HSLUEBufWWtSbTCcAoLG+EyKehLZTxUlOJGl+DJC646dluTWJfdkIbnh3u7lAyBuTC5xarqSkwX5MH0VgSZch2R2TcedEr0C0q6C7QiWp5TU0igPODecX2tYDFw6klcB/bB/o+9AgDaYe7pGwCGDFm50I0Mi3/YuszdA8SK2TQEDpfor9qwzU2LdX4KqnGyNYwT9FTfCDQ==
     */
    private static final String TX_METADATA_CERTIFICATE_BASE64 = "REPLACE_ME_BASE64_PS_CERTIFICATE";

    /** to verify service from payload data signature in messages
     * example: SIGNING_SERVICE
     */
    private static final String SERVICE_NAME = "replace_me_service_name";


    public static void main(String[] args) throws IOException {

        String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        JvmInput jvmInput = mapper.readValue(input, JvmInput.class);

        try {
            Task.TaskLevel6 taskLevel = mapper.readValue(jvmInput.getInput(), Task.TaskLevel6.class);
            if (taskLevel.getApprovalToBeSigned() == null || taskLevel.getApprovalToBeSigned().isEmpty()) {
                throw new RuntimeException("ApprovalToBeSigned is not found in input");
            }
            String approvalToBeSigned = taskLevel.getApprovalToBeSigned();

            String base64Metadata = taskLevel.getMetaData();
            String base64MetadataSignature = taskLevel.getMetaDataSignature();
            byte[] metadata = Base64.getDecoder().decode(base64Metadata.getBytes());
            byte[] metadataSignature = Base64.getDecoder().decode(base64MetadataSignature.getBytes());
            if (metadata == null) {
                throw new RuntimeException("Input is invalid: Metadata is missing or empty.");
            }

            if (metadataSignature == null) {
                throw new RuntimeException("Input is invalid: MetadataSignature is missing or empty.");
            }

            // --- 2. PARSE THE METADATA ---
            JsonNode metadataNode = mapper.readTree(metadata);

            if (metadataNode.isNull()) {
                throw new RuntimeException("Input is invalid: 'payload' field is missing or not text in metadata.");
            }

            JsonNode messagesToSign = metadataNode.get("messagesToSign");
            String algorithm = metadataNode.get("algorithm").asText();
            String keyId = metadataNode.get("signingDeviceKeyId").asText();

            String payload = metadataNode.get("rawPayload").asText();

            JsonNode payloadNode = mapper.readTree(payload);
            JsonNode fireblocksMetadata = payloadNode.get("metadata");

            String serviceName = metadataNode.get("serviceName").asText();

            // --- 3. VERIFY SIGNATURES ---
            PublicKey payloadCertPublicKey = getPublicKey(PAYLOAD_CERTIFICATE_BASE64);

            Signature sigPayload = Signature.getInstance("SHA256withRSA");
            sigPayload.initVerify(payloadCertPublicKey);
            sigPayload.update(payload.getBytes(StandardCharsets.UTF_8));

            boolean validPayload = sigPayload.verify(metadataSignature);

            if (!validPayload){
                throw new RuntimeException("Input is invalid: payload signature verification failed.");
            }

            if (fireblocksMetadata == null || fireblocksMetadata.isNull()) {
                throw new IllegalArgumentException("Missing fireblocks metadata");
            }

            String txMetadata = null;
            JsonNode txMetadataNode = fireblocksMetadata.get("txMetaData");
            if (txMetadataNode != null && !txMetadataNode.isNull()) {
                txMetadata = txMetadataNode.asText();
            } else {
                throw new IllegalArgumentException("Missing fireblocks txMetadata");
            }

            String txMetadataSignatureHex = null;
            JsonNode signaturesArray = fireblocksMetadata.get("txMetaDataSignatures");
            if (signaturesArray != null && signaturesArray.isArray() && !signaturesArray.isEmpty()) {
                JsonNode firstSig = signaturesArray.get(0).get("signature");
                if (firstSig != null && !firstSig.isNull()) {
                    txMetadataSignatureHex = firstSig.asText();
                }
            } else {
                throw new IllegalArgumentException("Missing fireblocks txMetadata signature");
            }

            byte[] txMetadataSignatureBytes = null;
            if (txMetadataSignatureHex != null) {
                txMetadataSignatureBytes = HexFormat.of().parseHex(txMetadataSignatureHex);
            }

            PublicKey txMetadataCertPublicKey = getPublicKey(TX_METADATA_CERTIFICATE_BASE64);

            Signature sigTxMetadata = Signature.getInstance("SHA256withRSA");
            sigTxMetadata.initVerify(txMetadataCertPublicKey);
            sigTxMetadata.update(txMetadata.getBytes(StandardCharsets.UTF_8));

            boolean validTxMetadata = sigTxMetadata.verify(txMetadataSignatureBytes);

            if (!validTxMetadata){
                throw new RuntimeException("Input is invalid: txMetadata signature verification failed.");
            }


            // --- 4. VALIDATE METADATA ---
            if (messagesToSign.isNull() || !messagesToSign.isArray()) {
                throw new RuntimeException("Input is invalid: 'messagesToSign' field is missing or not text in metadata.");
            }

            if (!serviceName.equals(SERVICE_NAME)){
                throw new RuntimeException("Input is invalid: 'service' type is incorrect.");
            }

            boolean matchFound = false;

            for (JsonNode msgNode : messagesToSign) {
                String message = msgNode.path("message").asText(null);
                if (taskLevel.getPayload().equals(message)) {
                    matchFound = true;
                    break;
                }
            }

            if (!matchFound){
                throw new RuntimeException("Input is invalid: Metadata message is not equal payload.");
            }

            if (!keyId.equals(taskLevel.getSignedSignRequest().getSignRequest().getSignKeyName())){
                throw new RuntimeException("Input is invalid: signingDeviceKeyId is not equal signKeyName.");
            }

            String tsbSigningAlgorithm;
            switch (algorithm) {
                case "ECDSA_SECP256K1" -> tsbSigningAlgorithm = "NONEwithECDSA";
                case "EDDSA_ED25519" -> tsbSigningAlgorithm = "EdDSA";
                default -> throw new RuntimeException("Unsupported algorithm in metaData: " + algorithm);
            }

            if (!tsbSigningAlgorithm.equals(taskLevel.getSignedSignRequest().getSignRequest().getSignatureAlgorithm().getAlgorithm())){
                throw new RuntimeException("Input is invalid: signingDeviceKeyId is not equal signKeyName.");
            }

            // no violations, sign the challenge
            byte[] decodedApprovalToBeSigned = Base64.getDecoder().decode(approvalToBeSigned);
            System.out.write(decodedApprovalToBeSigned);
            System.out.flush();
            System.exit(0);
        } catch (IOException | NoSuchAlgorithmException | SignatureException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private static PublicKey getPublicKey(String certificateBase64String) {

        try {
            PublicKey publicKey;
            String normalized = certificateBase64String
                    .replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "")
                    .replaceAll("\\s+", "");

            byte[] certBytes = Base64.getDecoder().decode(normalized);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
            publicKey = certificate.getPublicKey();

            return publicKey;
        } catch (CertificateException e) {
            throw new IllegalArgumentException("Invalid certificate provided");
        }
    }

}
