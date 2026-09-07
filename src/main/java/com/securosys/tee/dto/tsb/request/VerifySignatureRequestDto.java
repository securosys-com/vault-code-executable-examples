/**
 * Copyright (c)2005 Securosys SA, authors: Tomasz Madej
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
package com.securosys.tee.dto.tsb.request;

import com.securosys.tee.enums.tsb.SignatureAlgorithm;
import com.securosys.tee.validation.tsb.Base64Encoded;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Verify signature request.")
public class VerifySignatureRequestDto {

    @NotEmpty
    @Schema(description = "Name of the key with which the payload was signed. For signature verification of a derived key, append the full derivation path to the key name, e.g. MyKey/1/2/3.")
    private String signKeyName;

    @Base64Encoded
    @Schema(description = "External key object with which the request shall be verified. Subscription for External Keystore required.", format = "base64")
    private String signKeyObject;

    @ArraySchema(arraySchema = @Schema(description = "The password of the master key, if the master key has a password set."), schema = @Schema(pattern = "^.{1}$"))
    private char[] masterKeyPassword;

    @NotNull
    @Schema(description = "The signature algorithm that was used to sign the payload.")
    private SignatureAlgorithm signatureAlgorithm;

    @NotEmpty
    @Base64Encoded
    @Schema(description = "Payload (base64 encoded) for which the signature was created.", format = "byte")
    private String payload;

    @NotEmpty
    @Base64Encoded
    @Schema(description = "The signature to be verified.", format = "byte")
    private String signature;

    public String getSignKeyName() {
        return signKeyName;
    }

    public void setSignKeyName(String signKeyName) {
        this.signKeyName = signKeyName;
    }

    public String getSignKeyObject() {
        return signKeyObject;
    }

    public void setSignKeyObject(String signKeyObject) {
        this.signKeyObject = signKeyObject;
    }

    public char[] getMasterKeyPassword() {
        return masterKeyPassword;
    }

    public void setMasterKeyPassword(char[] masterKeyPassword) {
        this.masterKeyPassword = masterKeyPassword;
    }

    public SignatureAlgorithm getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(SignatureAlgorithm signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
