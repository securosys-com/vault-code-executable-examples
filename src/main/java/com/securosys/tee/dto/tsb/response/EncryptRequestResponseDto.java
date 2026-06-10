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
package com.securosys.tee.dto.tsb.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response for an encrypt request.")
public class EncryptRequestResponseDto {

    @Schema(description = "The encrypted payload.")
    private String encryptedPayload;

    @Schema(description = "The encrypted payload without message authentication code (MAC) as part of the AES-GCM encryption process.")
    private String encryptedPayloadWithoutMessageAuthenticationCode;

    @Schema(description = "The initialization vector (base64 encoded) used to encrypt the payload. Is empty if the algorithm "
            + "used does not require an initialization vector.", nullable = true)
    private String initializationVector;

    @Schema(description = "The authenticationTag is a message authentication code (MAC) as part of the AES-GCM encryption process. " +
            "It is used to ensure the integrity of the ciphertext and the additional authenticated data (AAD) passed to the encrypt operation. " +
            "The MAC is part of the cipher text and is additionally returned here. The cipher text contains the MAC and must be truncated depending on the framework used.", nullable = true, format = "base64")
    private String messageAuthenticationCode;

    public String getEncryptedPayload() {
        return encryptedPayload;
    }

    public void setEncryptedPayload(String encryptedPayload) {
        this.encryptedPayload = encryptedPayload;
    }

    public String getInitializationVector() {
        return initializationVector;
    }

    public void setInitializationVector(String initializationVector) {
        this.initializationVector = initializationVector;
    }

    public String getMessageAuthenticationCode() {
        return this.messageAuthenticationCode;
    }

    public void setMessageAuthenticationCode(String messageAuthenticationCode) {
        this.messageAuthenticationCode = messageAuthenticationCode;
    }

    public String getEncryptedPayloadWithoutMessageAuthenticationCode() {
        return this.encryptedPayloadWithoutMessageAuthenticationCode;
    }

    public void setEncryptedPayloadWithoutMessageAuthenticationCode(String payloadWithouthMAC) {
        this.encryptedPayloadWithoutMessageAuthenticationCode = payloadWithouthMAC;
    }
}
