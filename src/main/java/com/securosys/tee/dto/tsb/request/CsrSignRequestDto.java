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

import com.securosys.tee.enums.tsb.ExtendedKeyUsage;
import com.securosys.tee.enums.tsb.KeyUsage;
import com.securosys.tee.enums.tsb.SignatureAlgorithm;
import com.securosys.tee.validation.tsb.Base64Encoded;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "CSR request with Smart Key Attributes.")
public class CsrSignRequestDto {

    @NotEmpty
    @Schema(description = "The name of the key used to sign the certificate request.")
    private String signKeyName;

    @ArraySchema(arraySchema = @Schema(description = "The password of the key"), schema = @Schema(pattern = "^.{1}$"))
    private char[] keyPassword;

    @NotNull
    @Schema(description = "The signature algorithm to be used. The chosen algorithm has to be compatible with the type of the "
            + "key referenced by the signKeyName.")
    private SignatureAlgorithm signatureAlgorithm;

    @NotNull
    @Schema(description = "The days from today after which the certificate is not valid. e.g. 365 //valid for 1 year.")
    private int validity;

    @NotNull
    @Schema(description = "The standard certificate request attributes for X.500 series")
    private CertificateAttributesDto standardCertificateAttributes;

    @Schema(description = "The key usage extension defines the purpose (for example, encipherment, signature, or certificate" +
            " signing) of the key contained in the certificate. If the public key is used for entity authentication, " +
            "then the certificate extension should have the key usage Digital signature.")
    private List<KeyUsage> keyUsage;

    @Schema(description = "This extension indicates one or more purposes for which the certified public key may be used, in" +
            " addition to or in place of the basic purposes indicated in the key usage extension. In general, " +
            "this extension will appear only in end entity certificates.")
    private List<ExtendedKeyUsage> extendedKeyUsage;

    @Base64Encoded
    @Schema(description = "Additional meta data that will be provided to the approval client.", format = "base64")
    private String metaData;

    @Base64Encoded
    @Schema(description = "Signature for the meta data.", format = "base64")
    private String metaDataSignature;
}
