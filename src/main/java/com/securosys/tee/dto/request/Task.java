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
package com.securosys.tee.dto.request;

import com.securosys.tee.dto.tsb.request.*;
import com.securosys.tee.dto.tsb.response.RequestStatusDto;
import com.securosys.tee.enums.tsb.PayloadType;
import com.securosys.tee.enums.tsb.SignatureAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class Task {
    @Data
    public static class TaskLevel {
        private String id;
        private String detailLevel;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class TaskLevel1 extends TaskLevel {
        private String approvalToBeSigned;
        private String approverPublicKey;
    }


    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class TaskLevel2 extends TaskLevel1 {
        private String metaData;
        private String metaDataSignature;
        private String payload;
        private PayloadType payloadType;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class TaskLevel3 extends TaskLevel2 {
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


    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class TaskLevel4 extends TaskLevel3 {
        private String timestamp;
        private String timestampSignature;
        private SignatureAlgorithm timestampSignatureAlgorithm;
        private String timestampSignatureWithAlgorithm;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class TaskLevel5 extends TaskLevel4 { //NOSONAR: Inheritance depth is desired here
        private String keyAttributes;
        private String keyAttributesSignature;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class TaskLevel6 extends TaskLevel5 { //NOSONAR: Inheritance depth is desired here

        private RequestStatusDto requestStatus;
    }
}

