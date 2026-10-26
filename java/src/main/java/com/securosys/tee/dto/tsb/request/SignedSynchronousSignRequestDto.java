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
import jakarta.validation.constraints.NotNull;

@Schema(description = "Contains a synchronous sign request and optionally the requests signature. " +
        "Character '/' is used for key-derivation and should be avoided using in keyName, encode the keyname contains '/'.")
public class SignedSynchronousSignRequestDto {

    @NotNull
    @Valid
    private SynchronousSignRequestDto signRequest;

    @Valid
    private SignatureDto requestSignature;

    public SynchronousSignRequestDto getSignRequest() {
        return signRequest;
    }

    public void setSignRequest(SynchronousSignRequestDto signRequest) {
        this.signRequest = signRequest;
    }

    public SignatureDto getRequestSignature() {
        return requestSignature;
    }

    public void setRequestSignature(SignatureDto requestSignature) {
        this.requestSignature = requestSignature;
    }

}
