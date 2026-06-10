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
import com.securosys.tee.validation.tsb.ISO8601Timestamp;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

@Schema(description = "Request that can be authenticated.")
public abstract class AuthenticatableRequestDto {

    @NotEmpty
    @Schema(description = "The ISO-8601 formatted timestamp that has been signed by the approval client.", format = "date-time")
    @ISO8601Timestamp
    private String timestamp;

    @NotEmpty
    @Base64Encoded
    @Schema(description = "Signature(base64 encoded)  for the timestamp (ISO-8601) that was done using the key of the approver." +
            " Format of the signature is depending on the algorithm used and as returned when using a JDK's native Signature.sign() method."
            + "<br>"
            + "Example (EC key):<br>"
            + "echo \"MEUCIDOsDySFOWOcMKulHmR3uB8YCF7oX+vQ4dU8ooYC1OCvAiEApwMbu7nF19woZhO/KCIRV1api8oa/QCV2M2pcINAnEQ=\" | "
            + "base64 -D | openssl asn1parse -inform DER<br>"
            + "    0:d=0  hl=2 l=  69 cons: SEQUENCE<br>"
            + "    2:d=1  hl=2 l=  32 prim: INTEGER           "
            + ":33AC0F248539639C30ABA51E6477B81F18085EE85FEBD0E1D53CA28602D4E0AF<br>"
            + "   36:d=1  hl=2 l=  33 prim: INTEGER           :A7031BBBB9C5D7DC286613BF2822115756A98BCA1AFD0095D8CDA97083409C44",
            format = "base64")
    private String timestampSignature;

    @Base64Encoded
    @Schema(description = "Certificate used for the timestampSignature. Use when tasks for all approvers or a specific approver "
            + "shall be fetched, but without access to the approver's private key. To load all tasks neither the "
            + "approverPublicKey nor the approverCertificate may be set. If either one is set only the tasks for this "
            + "specific approver is loaded.", format = "base64")
    private String timestampSigningCertificate;

    @Base64Encoded
    @Schema(description = "Public key of the approver (base64 encoded). Either the approverPublicKey or the approverCertificate "
            + "has to be provided if the timestampSigningCertificate is not set.", format = "base64")
    private String approverPublicKey;

    @Base64Encoded
    @Schema(description = "Certificate of the approver in DER format (base64 encoded). Either the approverPublicKey or the "
            + "approverCertificate has to be provided if the timestampSigningCertificate is not set.", format = "base64")
    private String approverCertificate;

    @NotEmpty
    @Schema(description = "The message digest algorithm that was used for computing the timestamp signature.",
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
    private String timestampDigestAlgorithm;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestampSignature() {
        return timestampSignature;
    }

    public void setTimestampSignature(String timestampSignature) {
        this.timestampSignature = timestampSignature;
    }

    public String getTimestampSigningCertificate() {
        return timestampSigningCertificate;
    }

    public void setTimestampSigningCertificate(String timestampSigningCertificate) {
        this.timestampSigningCertificate = timestampSigningCertificate;
    }

    public String getApproverPublicKey() {
        return approverPublicKey;
    }

    public void setApproverPublicKey(String approverPublicKey) {
        this.approverPublicKey = approverPublicKey;
    }

    public String getApproverCertificate() {
        return approverCertificate;
    }

    public void setApproverCertificate(String approverCertificate) {
        this.approverCertificate = approverCertificate;
    }

    public String getTimestampDigestAlgorithm() {
        return timestampDigestAlgorithm;
    }

    public void setTimestampDigestAlgorithm(String timestampDigestAlgorithm) {
        this.timestampDigestAlgorithm = timestampDigestAlgorithm;
    }

    @Override
    public abstract String toString();
}

