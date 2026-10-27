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

import com.securosys.tee.enums.tsb.RequestStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response containing information about the status of a request.")
public class RequestStatusDto {

    @Schema(description = "The id of the request.")
    private String id;

    @Schema(description = "The current status of the request.")
    private RequestStatus status;

    @Schema(description = "Date and time when the request is sent to the hsm in ISO-8601 format.", nullable = true)
    private String executionTime;

    @ArraySchema(schema = @Schema(description = "Public keys of approvers that have approved the request."))
    private List<String> approvedBy;

    @ArraySchema(schema = @Schema(description = "Public keys of approvers that have not yet approved the request but still "
            + "could approve."))
    private List<String> notYetApprovedBy;

    @ArraySchema(schema = @Schema(description = "Public keys from approvers that have rejected to approve the request (i.e. "
            + "they deleted the task related to this request."))
    private List<String> rejectedBy;

    @Schema(description = "The result of the request after the request was executed on the hsm. In case of a sign request this "
            + "field contains the signature. In case of a decrypt request it contains the decrypted payload. Otherwise this "
            + "field is empty.", nullable = true)
    private String result;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public List<String> getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(List<String> approvedBy) {
        this.approvedBy = approvedBy;
    }

    public List<String> getNotYetApprovedBy() {
        return notYetApprovedBy;
    }

    public void setNotYetApprovedBy(List<String> notYetApprovedBy) {
        this.notYetApprovedBy = notYetApprovedBy;
    }

    public List<String> getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(List<String> rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "RequestStatusDto{" +
                "id='" + id + '\'' +
                ", status=" + status +
                ", executionTime='" + executionTime + '\'' +
                ", approvedBy=" + approvedBy +
                ", notYetApprovedBy=" + notYetApprovedBy +
                ", rejectedBy=" + rejectedBy +
                ", result='" + result + '\'' +
                '}';
    }
}
