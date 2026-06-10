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

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Synchronous sign request.")
public class SynchronousSignRequestDto {

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

    @Base64Encoded
    @Schema(description = "External key object with which the request shall be signed. Subscription for External Keystore required.", format = "base64")
    private String signKeyObject;

    @ArraySchema(arraySchema = @Schema(description = "Password of the sign key. If a derived key should be used for signing the "
            + "key password of the master key must be specified."), schema = @Schema(pattern = "^.{1}$"))
    private char[] keyPassword;

    /*
    MetaData and MetaDataSignature is completely useless here as no metadata can be displayed to an approval client because the
    request is synchronous (meaning instantly sent to the HSM) and therefore never displayed to an approval client. The reason why
    we have it in here anyways is that we used the same DTO for the synchronous and asynchronous sign request in the initial
    implementation. In the meantime we separated the synchronous sign request from a sign request as the synchronous sign request
    now has additional fields that a sign request does not need. This is also more inline with the other synchronous requests as
    they also have their own dto. Since we do not want to change the API and the initial implementation contained the MetaData
    fields we can not simply remove these fields anymore and are stuck with them. Maybe we can improve this if we ever offer
    v2 API endpoints or by disabling the FAIL_ON_UNKOWN_PROPERTIES configuration.
     */
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

    @Schema(description = "The type of the signature result. The result is in any case base64-encoded", example = "DER", allowableValues = {"ETH", "DER"})
    private String signatureType;

    @ArraySchema(schema = @Schema(description = "Signed approvals that are used to synchronously sign with a SKA key. "
            + SwaggerDocDescriptions.SIGNED_APPROVAL, format = "base64"))
    private List<@Base64Encoded String> signedApprovals = new ArrayList<>();

}

