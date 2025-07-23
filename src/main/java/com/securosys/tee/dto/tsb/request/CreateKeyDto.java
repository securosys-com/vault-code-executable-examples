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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.securosys.tee.dto.tsb.PolicyDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@SuppressWarnings("unused")
@JacksonXmlRootElement(localName = "private_key")
@Schema(description = "Create key request.")
@Data
public class CreateKeyDto {

    @NotEmpty
    @Schema(description = "The name of the key.")
    private String label;

    @ArraySchema(arraySchema = @Schema(description = "The password of the key"), schema = @Schema(pattern = "^.{1}$"))
    private char[] password;

    @Schema(description = "The id of the key, used if working with keys generated on TSB and used with PKCS#11 provider")
    private String id;

    @Schema(description = "The algorithm with which the key should be created. Either the algorithm or the algorithm_oid must "
            + "be set.", example = "EC", allowableValues = {"EC", "ED", "RSA", "DSA", "ISS", "BLS", "AES", "ChaCha20", "Camellia",
            "TDEA", "DILITHIUM_L2", "DILITHIUM_L3", "DILITHIUM_L5", "SPHINCS_PLUS_SHAKE_L1", "SPHINCS_PLUS_SHAKE_L3",
            "SPHINCS_PLUS_SHAKE_L5", "KYBER512_WITH_SHAKE", "KYBER768_WITH_SHAKE", "KYBER1024_WITH_SHAKE", "KYBER512_WITH_SHA2_AES",
            "KYBER768_WITH_SHA2_AES", "KYBER1024_WITH_SHA2_AES"})
    private String algorithm;

    @JacksonXmlProperty(localName = "algorithm_oid")
    @Schema(description = "The oid of the algorithm with which the key should be created. Either the algorithm or the "
            + "algorithm_oid must be set.", example = "1.2.840.10045.2.1")
    private String algorithmOid;

    @JacksonXmlProperty(localName = "curve_oid")
    @Schema(description = "The oid of the curve used for the EC or ED algorithm. Mandatory if chosen algorithm is set to EC or ED. " +
            "secp224k1: 1.3.132.0.32\n" +
            "secp224r1: 1.3.132.0.33\n" +
            "secp256k1: 1.3.132.0.10\n" +
            "secp256r1 (also known as P-256 or prime256v1): 1.2.840.10045.3.1.7\n" +
            "secp384r1 (also known as P-384): 1.3.132.0.34\n" +
            "secp521r1 (also known as P-521): 1.3.132.0.35\n" +
            "x962p239v1: 1.2.840.10045.3.1.1\n" +
            "x962p239v2: 1.2.840.10045.3.1.2\n" +
            "x962p239v3: 1.2.840.10045.3.1.3\n" +
            "brainpool224r1: 1.3.36.3.3.2.8.1.1.1\n" +
            "brainpool256r1: 1.3.36.3.3.2.8.1.1.7\n" +
            "brainpool320r1: 1.3.36.3.3.2.8.1.1.9\n" +
            "brainpool384r1: 1.3.36.3.3.2.8.1.1.11\n" +
            "brainpool512r1: 1.3.36.3.3.2.8.1.1.13\n" +
            "frp256v1: 1.2.250.1.223.101.256.1\n" +
            "Ed25519: 1.3.101.112",
            example = "1.3.132.0.10")
    private String curveOid;

    @JacksonXmlProperty(localName = "key_size")
    @Schema(description = "The length of the key. Only applicable for RSA , DSA and ISS. For ISS this attribute sets the "
            + "security level (1, 2 or 3).")
    private Integer keySize;

    @JacksonXmlProperty(localName = "address_truncated")
    private AddressFormatDto addressFormat;

    @NotNull
    @Valid
    private AttributesDto attributes;

    @Valid
    private PolicyDto policy;

}
