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

import com.securosys.tee.enums.tsb.CipherAlgorithm;
import com.securosys.tee.validation.tsb.Base64Encoded;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Synchronous decrypt request. Only supported by RSA keys.")
public class SynchronousDecryptRequestDto {

    @NotEmpty
    @Base64Encoded
    @Schema(description = "Encrypted payload (base64 encoded) that shall be decrypted.", format = "byte")
    private String encryptedPayload;

    @NotEmpty
    @Schema(description = "Name of the key with which the payload shall be decrypted.")
    private String decryptKeyName;

    @ArraySchema(arraySchema = @Schema(description = "Password of the decrypt key."), schema = @Schema(pattern = "^.{1}$"))
    private char[] keyPassword;

    @NotNull
    @Schema(description = "The cipher algorithm to be used.")
    private CipherAlgorithm cipherAlgorithm;

    @Schema(description = "The initialization vector (base64 encoded) used to encrypt the payload. Can be empty if the cipher "
            + "algorithm used does not require an initialization vector.")
    private String initializationVector;

    @Base64Encoded
    @Schema(description = "Additional authentication data (aad) used when decrypting payload. Can be empty if none were used "
            + "when encrypting the payload", format = "base64")
    private String additionalAuthenticationData;

    @ArraySchema(schema = @Schema(description = "Signed approvals that are used to synchronously decrypt with a SKA key. "
            + SwaggerDocDescriptions.SIGNED_APPROVAL, format = "base64"))
    private List<@Base64Encoded String> signedApprovals = new ArrayList<>();

    @Schema(description = "The MAC (Message Authentication Tag) is a fixed-length value that is included in the ciphertext and used to authenticate"
            + "the integrity of the data and the authenticity of the sender. \n"
            + "Supported tag_length: 0, 64, 96, 104, 112, 120, 128", format = "int")
    private int tagLength = 0;

}
