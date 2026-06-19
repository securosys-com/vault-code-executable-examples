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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Schema(description = "Request to filter the tasks.")
public class FilteredTasksRequestDto extends AuthenticatableRequestDto {

    @Schema(description = "If specified filters for a specific task id")
    private String id;

    @Schema(description = "If specified filters for a specific request id by approver Public Key. Request ID is ignored if task ID is set.")
    private String requestId;

    @Schema(description = "The detail level of the response.",
            allowableValues = {"level1", "level2", "level3", "level4", "level5"}, defaultValue = "level1")
    private String detailLevel = "level1";

    @Valid
    private PagingDto paging;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDetailLevel() {
        return detailLevel;
    }

    public void setDetailLevel(String detailLevel) {
        this.detailLevel = detailLevel;
    }

    public PagingDto getPaging() {
        return paging;
    }

    public void setPaging(PagingDto paging) {
        this.paging = paging;
    }

    @Override
    public String toString() {
        return "FilteredTasksRequestDto{" +
                "timestamp='" + getTimestamp() + '\'' +
                ", timestampSignature='" + getTimestampSignature() + '\'' +
                ", approverPublicKey='" + getApproverPublicKey() + '\'' +
                ", approverCertificate='" + getApproverCertificate() + '\'' +
                ", detailLevel='" + getDetailLevel() + '\'' +
                ", timestampDigestAlgorithm='" + getTimestampDigestAlgorithm() + '\'' +
                ", paging=" + getPaging() +
                '}';
    }
}

