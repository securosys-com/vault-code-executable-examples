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

import com.securosys.tee.enums.tsb.PayloadType;
import com.securosys.tee.enums.tsb.SignatureAlgorithm;
import com.securosys.tee.validation.tsb.Base64Encoded;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Sign request.")
public class SignRequestDto {

    @NotEmpty
    @Base64Encoded
    @Schema(description = "Payload that shall be signed.", format = "base64")
    private String payload;

    @Schema(description = "The type of the payload.", defaultValue = "UNSPECIFIED")
    private PayloadType payloadType = PayloadType.UNSPECIFIED;

    @NotEmpty
    @Schema(description = "Name of the key with which the request shall be signed. Note: to create a sign request for a BIP32 "
            + "derived key, make sure you have already created a master key that is BIP32 capable. Then use the master key name "
            + "and add the derivation path (e.g. \"TESTKEY/0'/1'/2\").")
    private String signKeyName;

    @ArraySchema(arraySchema = @Schema(description = "Password of the sign key. If a derived key should be used for signing "
            + "the key password of the master key must be specified."), schema = @Schema(pattern = "^.{1}$"))
    private char[] keyPassword;

    @Base64Encoded
    @Schema(description = "Additional meta data that will be provided to the approval client.", format = "base64")
    private String metaData;

    @Base64Encoded
    @Schema(description = "Signature for the meta data.", format = "base64")
    private String metaDataSignature;

    @NotNull
    @Schema(description = "The signature algorithm to be used. The chosen algorithm has to be compatible with the type of the "
            + "key referenced by the signKeyName param.")
    private SignatureAlgorithm signatureAlgorithm;

    @Schema(description = "The type of the signature result (default is DER), the rest-api can create V,R,S signature compatible with Ethereum." +
            " The result is in any case base64-encoded", example="DER", allowableValues = {"ETH", "DER"})
    private String signatureType;

    @Base64Encoded
    @Schema(description = "(Optional) Base64 encoded context bytes for ML-DSA & SLH-DSA, " +
            "which is included in the message hash computation, provides domain seperation, " +
            "binds signature to a specific use case (e.g., different protocols or applications, " +
            "prevents cross-protocol attacks.", format = "base64")
    private String context;

    @Schema(description = "(Optional) NONE_WITH_EC_SCHNORR_BIP0340 only, specify 'auxiliaryRandomData' (base64, 32 raw bytes), " +
            "if not specified, the HSM generates random aux_data", format = "base64")
    @Base64Encoded
    private String auxiliaryRandomData;

    @Schema(description = "(Optional) NONE_WITH_EC_SCHNORR_BIP0340 only, specify 'taprootTweakData' (base64, 32 raw bytes), " +
            "used in signing with key-path, if specified 'merkleRootData' must be empty.", format = "base64")
    @Base64Encoded
    private String taprootTweakData;

    @Schema(description = "(Optional) NONE_WITH_EC_SCHNORR_BIP0340  only, specify 'merkleRootData', " +
            "used in signing with script-path (multi-sig), if specified 'taprootTweakData' must be empty.", format = "base64")
    @Base64Encoded
    private String merkleRootData;

}

