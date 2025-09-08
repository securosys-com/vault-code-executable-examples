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

@Schema(description = "Request to submit the authorization token of an approval client.")
public class SendApprovalDto {

    @Schema(description = "Id of the task for which the approval is being submitted.")
    @NotEmpty
    private String id;

    @Schema(description = "The approval token as received in the task or constructed by the client using additional data "
            + "received in the task (base64 encoded)")
    @Base64Encoded
    @NotEmpty
    private String approvalToBeSigned;

    @Schema(description = "The signature of the approvalToBeSigned received with the task (base64 encoded)")
    @Base64Encoded
    @NotEmpty
    private String signature;

    @Schema(description = "The digest algorithm used for signing the approvalToBeSigned. The signature algorithm is given by "
            + "the approver's private key", example = "SHA-256",
            allowableValues = {MessageDigestAlgorithms.SHA_224,
                    MessageDigestAlgorithms.SHA_256,
                    MessageDigestAlgorithms.SHA_384,
                    MessageDigestAlgorithms.SHA_512,
                    MessageDigestAlgorithms.SHA3_224,
                    MessageDigestAlgorithms.SHA3_256,
                    MessageDigestAlgorithms.SHA3_384,
                    MessageDigestAlgorithms.SHA3_512,
            })
    @NotEmpty
    private String approvalDigestAlgorithm;

    @Schema(description = "If approver is public key based: The public key of the approver in the same format as provided "
            + "during key "
            + "creation (base64 encoded)")
    @Base64Encoded
    private String approverPublicKey;

    @Schema(description = "If approver is certificate based: The certificate of the approver in the same format as provided "
            + "during key creation (base64 encoded)")
    @Base64Encoded
    private String approverCertificate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApprovalToBeSigned() {
        return approvalToBeSigned;
    }

    public void setApprovalToBeSigned(String approvalToBeSigned) {
        this.approvalToBeSigned = approvalToBeSigned;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getApprovalDigestAlgorithm() {
        return approvalDigestAlgorithm;
    }

    public void setApprovalDigestAlgorithm(String approvalDigestAlgorithm) {
        this.approvalDigestAlgorithm = approvalDigestAlgorithm;
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

    @Override
    public String toString() {
        return "SendApprovalDto{" +
                "id='" + id + '\'' +
                ", approvalToBeSigned='" + approvalToBeSigned + '\'' +
                ", signature='" + signature + '\'' +
                ", approvalDigestAlgorithm='" + approvalDigestAlgorithm + '\'' +
                ", approverPublicKey='" + approverPublicKey + '\'' +
                ", approverCertificate='" + approverCertificate + '\'' +
                '}';
    }
}
