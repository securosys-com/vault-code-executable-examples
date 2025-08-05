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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.securosys.tee.dto.tsb.PolicyDto;
import com.securosys.tee.validation.tsb.Base64Encoded;
import com.securosys.tee.validation.tsb.EmptyIfAnotherFieldHasValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Key import request. For a symmetric key the privateKey and publicKey must be empty. For an asymmetric "
        + "key the secretKey must be empty")
@EmptyIfAnotherFieldHasValue(fieldName = "privateKey", dependFieldName = "secretKey")
@EmptyIfAnotherFieldHasValue(fieldName = "publicKey", dependFieldName = "secretKey")
@EmptyIfAnotherFieldHasValue(fieldName = "certificate", dependFieldName = "publicKey")
public class KeyImportDto {

    @NotEmpty
    @Schema(description = "The name of the key.")
    private String label;

    @NotEmpty
    @Schema(description = "The key algorithm.")
    private String algorithm;

    @JacksonXmlProperty(localName = "address_truncated")
    private AddressFormatDto addressFormat;

    @Base64Encoded
    @Schema(description = "The private key to be imported. The key must be encoded in the DER format including information "
            + "like the algorithm or the curve OID.", format = "base64")
    private String privateKey;

    @Base64Encoded
    @Schema(description = "The public key to be imported. The key must be encoded in the DER format including information "
            + "like the algorithm or the curve OID.", format = "base64")
    private String publicKey;

    @Base64Encoded
    @Schema(description = "The secret key to be imported", format = "base64")
    private String secretKey;

    @Base64Encoded
    @Schema(description = "Certificate that should be set to the imported key", format = "base64")
    private String certificate;

    @NotNull
    @Valid
    private AttributesDto attributes;

    @Valid
    private PolicyDto policy;

}
