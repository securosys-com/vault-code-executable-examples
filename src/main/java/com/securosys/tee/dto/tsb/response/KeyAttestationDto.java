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
import lombok.Data;

import java.util.List;

@Schema(description = "Response containing the signed key attributes.")
@Data
public class KeyAttestationDto {

    @Schema(description = "The name of the attestation key used to sign the attributes.")
    private String attestationKeyName;

    @Schema(description = "The xml representation of the key attributes.")
    private String signingKeyAttestationXml;

    @Schema(description = "The public key in PEM format to verify the signature")
    private String signingKeyPublicKey;

    @Schema(description = "The attestation-key certificate chain.")
    private List<String> signingKeyAttestationCertificate;

    @Schema(description = "The base64 encoded signature of the key-attributes signed by the attestation-key.")
    private String signingKeyAttestationSignatureBase64;
}
