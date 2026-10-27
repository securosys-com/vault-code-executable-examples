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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.securosys.tee.dto.tsb.request.*;
import com.securosys.tee.enums.tsb.PayloadType;
import com.securosys.tee.enums.tsb.SignatureAlgorithm;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "Response containing the requested tasks with the specified detail level.")
public class GetTasksResponseDto {

    @ArraySchema(schema = @Schema(description = "The requested tasks."))
    private List<TaskLevel> tasks;

    public List<TaskLevel> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskLevel> tasks) {
        this.tasks = tasks;
    }

    @Schema(description = "Base Task Object")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "detailLevel", visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TaskLevel1.class, name = "level1"),
            @JsonSubTypes.Type(value = TaskLevel2.class, name = "level2"),
            @JsonSubTypes.Type(value = TaskLevel3.class, name = "level3"),
            @JsonSubTypes.Type(value = TaskLevel4.class, name = "level4"),
            @JsonSubTypes.Type(value = TaskLevel5.class, name = "level5"),
            @JsonSubTypes.Type(value = TaskLevel6.class, name = "level6")
    })
    @JsonIgnoreProperties(value = {"detailLevel"})
    @Data
    public static class TaskLevel {

        @Schema(description = "Id of the task.")
        private String id;
    }

    @Schema(description = "Task with detail level 1.")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TaskLevel1 extends TaskLevel {

        @Schema(description = "Already prepared ready to sign serialization of the approval token. The approvalToBeSigned is a "
                + "binary structure with the following format.<br><br>"
                + "Example:<br>"
                + "Base64: /AAAADsABAABAAAAAhApAFRFU1RLRVkyMDIwLTAzLTE2XzEwLTQxLTA2LjM4NDY2MDA0MzcxMDAwAAAAVBBEAEAAAABXEBYAQXBwcm92ZVNpZ25UYXNrUGF5bG9hZAAABwEIALRJb14AAAAAAhASAHRlc3QtdGltZXN0YW1wLWtleQAAVhBbADBZMAwGCCqGSM49BAMCBQADSQAwRgIhAKLnz8NtSThbVkSMjj8Zwl45vmyQ4bL/XMbLxV0RYa5mAiEAudcZnBwJ8SAXvTpcgvbP4wPbID8/Ia+vmcoRqAlXjlkAVxAWAEFwcHJvdmVTaWduVGFza1BheWxvYWQAAA=="
                + "<br>"
                + "Hex: FC0000003B0004000100000002102900544553544B4559323032302D30332D31365F31302D34312D30362E3338343636303034333731303030000000541044004000000057101600417070726F76655369676E5461736B5061796C6F6164000007010800B4496F5E0000000002101200746573742D74696D657374616D702D6B6579000056105B003059300C06082A8648CE3D04030205000349003046022100A2E7CFC36D49385B56448C8E3F19C25E39BE6C90E1B2FF5CC6CBC55D1161AE66022100B9D7199C1C09F12017BD3A5C82F6CFE303DB203F3F21AFAF99CA11A809578E590057101600417070726F76655369676E5461736B5061796C6F61640000"
                + "<br>"
                + "<br>"
                + "FC000000<br>"
                + "<i>length: 252byte</i><br>"
                + "<br>"
                + "3B00<br>"
                + "<i>type: 59d = EkaOperation</i><br>"
                + "<br>"
                + "0400<br>"
                + "<i>length: = 4byte</i><br>"
                + "<br>"
                + "01000000<br>"
                + "<i>data: Operation Type: \"OPERATION\", could also be \"BLOCK\", \"UNBLOCK\" or \"MODIFY\"</i><br>"
                + "<br>"
                + "0210<br>"
                + "<i>type: 4098d = LabelUtf8String</i><br>"
                + "<br>"
                + "2900<br>"
                + "<i>length: 41byte</i><br>"
                + "<br>"
                + "544553544B4559323032302D30332D31365F31302D34312D30362E3338343636303034333731303030000000<br>"
                + "<i>data: label of the key = \"TESTKEY2020-03-16_10-41-06.38466004371000\" (incl. padding for 4byte alignment)</i><br>"
                + "<br>"
                + "5410<br>"
                + "<i>type: 4180d = EkaTimeStamp</i><br>"
                + "<br>"
                + "4400<br>"
                + "<i>length: 68byte</i><br>"
                + "<br>"
                + "4000000057101600417070726F76655369676E5461736B5061796C6F6164000007010800B4496F5E0000000002101200746573742D74696D657374616D702D6B65790000<br>"
                + "<i>data: timestamp (see TaskLevel4 for details)</i><br>"
                + "<br>"
                + "5610<br>"
                + "<i>type: 4182d = DerSignature</i><br>"
                + "<br>"
                + "5B00<br>"
                + "<i>length: 91byte</i><br>"
                + "<br>"
                +
                "3059300C06082A8648CE3D04030205000349003046022100A2E7CFC36D49385B56448C8E3F19C25E39BE6C90E1B2FF5CC6CBC55D1161AE66022100B9D7199C1C09F12017BD3A5C82F6CFE303DB203F3F21AFAF99CA11A809578E5900"
                + "<i><br>data: signature of the timestamp (see TaskLevel4 for details)</i><br>"
                + "<br>"
                + "5710<br>"
                + "<i>type: 4183d = EkaSignPayload (could also be EkaModifyPayload)</i><br>"
                + "<br>"
                + "1600<br>"
                + "<i>length: 22bytes</i><br>"
                + "<br>"
                + "417070726F76655369676E5461736B5061796C6F61640000<br>"
                + "<i>data: the payload to be signed</i>",
                format = "base64")
        private String approvalToBeSigned;

        @Schema(description = "The public key of the approver for whom the task was created.")
        private String approverPublicKey;
    }


    @Schema(description = "Task with detail level 2.")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TaskLevel2 extends TaskLevel1 {

        @Schema(description = "Additional meta data (base64 encoded) as provided by the sign requester.", format = "byte")
        private String metaData;

        @Schema(description = "Signature for the meta data (base64 encoded).", format = "byte")
        private String metaDataSignature;

        @Schema(description = "Payload (base64 encoded) that shall be signed.", format = "byte")
        private String payload;

        @Schema(description = "The type of the payload.")
        private PayloadType payloadType;
    }

    @Schema(description = "Task with detail level 3.")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TaskLevel3 extends TaskLevel2 {

        @Schema(description = "The original request as sent by the caller. To be used for signature verification.")
        private String requestBase64;

        private SignedSignRequestDto signedSignRequest;

        private SignedModifyKeyRequestDto signedModifyKeyRequest;

        private SignedBlockKeyRequestDto signedBlockKeyRequest;

        private SignedUnblockKeyRequestDto signedUnblockKeyRequest;

        private SignedDecryptRequestDto signedDecryptRequest;

        private SignedUnwrapKeyRequestDto signedUnwrapKeyRequest;

        private SignedCertificateRequestRequestDto signedCertificateRequestRequest;

        private SignedSignCertificateRequestDto signedSignCertificateRequest;

        private SignedSelfSignCertificateRequestDto signedSelfSignCertificateRequestDto;
    }


    @Schema(description = "Task with detail level 4.")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TaskLevel4 extends TaskLevel3 {

        @Schema(description = "Timestamp as provided by the HSM. For timestamps a TLV (type length value) format is used. See "
                + "the following example on how to decode a timestamp provided by the HSM. <i>Please note that little endian "
                + "encoding is used and padding is 4 byte.</i><br>"
                + "Example:<br>"
                + "64 00 00 00<br><i>length 100 byte</i><br><br>"
                + "57 10<br><i>type 0x1057=EkaUsagePayload (could also be 0x1058 EkaModifyPayload)</i><br><br>"
                + "16 00<br><i>length=22 byte</i><br><br>"
                + "54 68 69 73 49 73 4A 75 73 74 41 50 61 79 6C 6F 61 64 54 65 73 74<br><i>payload</i><br><br>"
                + "00 00<br><i>padding</i><br><br>"
                + "0701<br><i>type 0x0107=Seconds since epoch</i><br><br>"
                + "0800<br><i>length 8 byte</i><br><br>"
                + "82 CE 5F 5E 00 00 00 00<br><i>seconds since epoch</i><br><br>"
                + "0210<br><i>type 0x1002=LABEL_UTF8</i><br><br>"
                + "3500<br><i>length 53 byte</i><br><br>"
                + "696E746567726974794B65794E616D652D35656431303438662D353864372D346237302D386166342D643363333735666239636337"
                + "<br><i>label = 'integrityKeyName-5ed1048f-58d7-4b70-8af4-d3c375fb9cc7'</i><br><br>"
                + "000000<br><i>padding</i>",
                format = "base64")
        private String timestamp;

        @Schema(description = "Signature of the timestamp as provided by the HSM (using integrity key). Signature is DER "
                + "encoded and can directly be used in e.g. the Java signature verifier.<br>"
                + "Example (ECDSA):<br>"
                + "echo \"MEYCIQCx7KRl7jrxsiNfMQ19ltO0/0sbFsQY1fMvJ7SXfCrDnwIhAOgGm3Un8QQA9vreOB62/lo+Dgs5EoZzWhcjZ4/6OQyN\" | "
                + "base64 -D | openssl asn1parse -inform DER -i<br>"
                + "    0:d=0  hl=2 l=  70 cons: SEQUENCE<br>"
                + "    2:d=1  hl=2 l=  33 prim:  INTEGER           "
                + ":B1ECA465EE3AF1B2235F310D7D96D3B4FF4B1B16C418D5F32F27B4977C2AC39F<br>"
                + "   37:d=1  hl=2 l=  33 prim:  INTEGER           "
                + ":E8069B7527F10400F6FADE381EB6FE5A3E0E0B391286735A1723678FFA390C8D",
                format = "base64")
        private String timestampSignature;

        @Schema(description = "The algorithm that was used to sign the timestamp")
        private SignatureAlgorithm timestampSignatureAlgorithm;

        @Schema(description = "DER encoded signature of the timestamp including the algorithm used. This representation is "
                + "expected as part of the approval token signed by the approver. It is a combination of 'timestampSignature' "
                + "and 'timestampSignatureAlgorithm' and provided for convenience.<br>"
                + "Example (ECDSA)<br>"
                + "echo \"MFkwDAYIKoZIzj0EAwIFAANJADBGAiEAseykZe468bIjXzENfZbTtP9LGxbEGNXzLye0l3wqw58CIQDoBpt1J"
                + "/EEAPb63jgetv5aPg4LORKGc1oXI2eP+jkMjQ==\" | base64 -D | openssl asn1parse -inform DER -i<br>"
                + "    0:d=0  hl=2 l=  89 cons: SEQUENCE<br>"
                + "    2:d=1  hl=2 l=  12 cons:  SEQUENCE<br>"
                + "    4:d=2  hl=2 l=   8 prim:   OBJECT            :ecdsa-with-SHA256<br>"
                + "   14:d=2  hl=2 l=   0 prim:   NULL<br>"
                + "   16:d=1  hl=2 l=  73 prim:  BIT STRING", format = "base64")
        private String timestampSignatureWithAlgorithm;
    }

    @Schema(description = "Task with detail level 5.")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TaskLevel5 extends TaskLevel4 { //NOSONAR: Inheritance depth is desired here

        @Schema(description = "The key attributes for the signing key in XML format.")
        private String keyAttributes;

        @Schema(description = "Signature for the key attributes as provided by the HSM (base64 encoded).",
                format = "byte")
        private String keyAttributesSignature;
    }

    @Schema(description = "Task with detail level 6.")
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TaskLevel6 extends TaskLevel5 { //NOSONAR: Inheritance depth is desired here

        private RequestStatusDto requestStatus;
    }
}

