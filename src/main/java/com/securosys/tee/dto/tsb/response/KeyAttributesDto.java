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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.securosys.tee.dto.tsb.PolicyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@SuppressWarnings("unused")
@JacksonXmlRootElement(localName = "private_key")
@Schema(description = "Response containing information about the key.")
public class KeyAttributesDto {

    @Schema(description = "The name of the key.")
    private String label;

    @Schema(description = "The id of the key.", nullable = true)
    private String id;

    @Schema(description = "The id of the external key.", nullable = true)
    private String uuid;

    @Schema(description = "The algorithm with which the key was created.", example = "EC")
    private String algorithm;

    @JacksonXmlProperty(localName = "algorithm_oid")
    @Schema(description = "The oid of the algorithm with which the key was created.", example = "1.2.840.10045.2.1")
    private String algorithmOid;

    @JacksonXmlProperty(localName = "curve_oid")
    @Schema(description = "The oid of the curve used for the EC algorithm (only set if the algorithm is EC).",
            example = "1.3.132.0.34", nullable = true)
    private String curveOid;

    @JacksonXmlProperty(localName = "derivation_value")
    @Schema(description = "The derived key attributes", nullable = true)
    private DerivedKeyAttributesDto derivedAttributes;

    @JacksonXmlProperty(localName = "key_size")
    @Schema(description = "The length of the key. Only set if the algorithm is RSA, DSA or ISS. For ISS this attribute "
            + "represents the security level.")
    private Integer keySize;

    @JacksonXmlProperty(localName = "create_time")
    @Schema(description = "The time at which the key was created on the HSM.")
    private String createTime;

    @JacksonXmlProperty(localName = "attest_time")
    @Schema(description = "The time at which the key was attested on the HSM.")
    private String attestTime;

    @JacksonXmlProperty(localName = "public_key")
    @Schema(description = "The public key from the created private key.\nThe public key is base64(DER/ASN.1) encoded, as "
            + "supported by e.g. OpenSSL, Java and many other languages/tools.\n"
            + "Decode using OpenSSL:\n"
            + "echo -n $EXAMPLE | base64 -D | openssl asn1parse -inform DER -dump", nullable = true)
    private String publicKey;

    @JacksonXmlProperty(localName = "address_truncated")
    private AddressTruncatedDto addressTruncated;

    private Attributes attributes;

    private PolicyDto policy;

    @Data
    @Schema(description = "The attributes of a key.")
    public static class Attributes {

        @Schema(description = "If true the key can be used to decrypt data.")
        private Boolean decrypt;

        @Schema(description = "If true the key can sign.")
        private Boolean sign;

        @JacksonXmlProperty(localName = "eka_sign")
        @Schema(nullable = true)
        //TODO 13-Jan 2020/wapa: When is ekaSign set to true and when to false? -> add description
        private Boolean ekaSign;

        @Schema(description = "If true the key can be used to unwrap keys.")
        private Boolean unwrap;

        @Schema(description = "If true it is possible to derive from this key.")
        private Boolean derive;

        @Schema(description = "If true the key is sensitive.")
        private Boolean sensitive;

        @JacksonXmlProperty(localName = "always_sensitive")
        @Schema(description = "Is true if the key was always sensitive since its creation.")
        private Boolean alwaysSensitive;

        @Schema(description = "If true the key is extractable. Keys with smart key attributes are never extractable.")
        private Boolean extractable;

        @JacksonXmlProperty(localName = "never_extractable")
        @Schema(description = "Is true if the key was never extractable since its creation.")
        private Boolean neverExtractable;

        @Schema(description = "If true the key can be modified.")
        private Boolean modifiable;

        @Schema(description = "If true the encrypted key is stored in an external memory")
        private Boolean copyable;

        @Schema(description = "If true the key can be deleted.")
        private Boolean destroyable;
    }
}
