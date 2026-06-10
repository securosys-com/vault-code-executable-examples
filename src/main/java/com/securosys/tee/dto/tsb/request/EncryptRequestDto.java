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

@Data
@Schema(description = "Encrypt request.")
public class EncryptRequestDto {

    @NotEmpty
    @Base64Encoded
    @Schema(description = "Payload (base64 encoded) that shall be encrypted.", format = "byte")
    private String payload;

    @NotEmpty
    @Schema(description = "Name of the key with which the payload shall be encrypted.")
    private String encryptKeyName;

    @ArraySchema(arraySchema = @Schema(description = "Password of the encrypt key. This is only necessary if the key algorithm "
            + "is symmetric."), schema = @Schema(pattern = "^.{1}$"))
    private char[] keyPassword;

    @NotNull
    @Schema(description = "The cipher algorithm to be used.")
    private CipherAlgorithm cipherAlgorithm;

    @Base64Encoded
    @Schema(description = "Additional authentication data (aad, base64 encoded) used for encryption with algorithm that support them.", format = "base64")
    private String additionalAuthenticationData;

    @Schema(description = "The MAC (Message Authentication Tag) is a fixed-length value as part of the AES-GCM encryption process, that is INCLUDED in the ciphertext and used to authenticate"
            + "the integrity of the data and the authenticity of the sender. \n"
            + "Supported tag_length: 0, 64, 96, 104, 112, 120, 128", format = "int")
    private int tagLength = 0;

}
