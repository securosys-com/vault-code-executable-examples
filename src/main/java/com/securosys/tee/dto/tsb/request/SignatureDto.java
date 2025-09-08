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

import com.securosys.tee.validation.tsb.Base64Encoded;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

@Schema(description = "Contains the signature and information how the signature was calculated.")
public class SignatureDto {

    @NotEmpty
    @Base64Encoded
    @Schema(description = "Signature for a JSON object (base64 encoded).<br>Format of the signature is depending on the "
            + "algorithm used and as returned when using a JDK's native Signature.sign() method.<br>"
            + "Example (EC key):<br>"
            + "echo \"MEUCIDOsDySFOWOcMKulHmR3uB8YCF7oX+vQ4dU8ooYC1OCvAiEApwMbu7nF19woZhO/KCIRV1api8oa/QCV2M2pcINAnEQ=\" | "
            + "base64 -D | openssl asn1parse -inform DER<br>"
            + "    0:d=0  hl=2 l=  69 cons: SEQUENCE<br>\"\n"
            + "    2:d=1  hl=2 l=  32 prim: INTEGER           "
            + ":33AC0F248539639C30ABA51E6477B81F18085EE85FEBD0E1D53CA28602D4E0AF<br>"
            + "   36:d=1  hl=2 l=  33 prim: INTEGER           :A7031BBBB9C5D7DC286613BF2822115756A98BCA1AFD0095D8CDA97083409C44",
            format = "base64")
    private String signature;

    @NotEmpty
    @Schema(description = "The message digest algorithm that was used for computing the request signature.",
            example = "SHA-256",
            allowableValues = {MessageDigestAlgorithms.SHA_224,
                    MessageDigestAlgorithms.SHA_256,
                    MessageDigestAlgorithms.SHA_384,
                    MessageDigestAlgorithms.SHA_512,
                    MessageDigestAlgorithms.SHA3_224,
                    MessageDigestAlgorithms.SHA3_256,
                    MessageDigestAlgorithms.SHA3_384,
                    MessageDigestAlgorithms.SHA3_512,
            })
    private String digestAlgorithm;

    @NotEmpty
    @Base64Encoded
    @Schema(description = "The public key (base64 encoded) that belongs to the private key used to calculate the signature.",
            format = "base64")
    private String publicKey;

}
