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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Creating an approver key-pair, based on approver name (key-name) and backup password.")
public class ApproverCreateDto {
    @NotEmpty
    @NotNull
    @Schema(description = "The name of the approver e.g. the e-mail address")
    private String approverName;

    @Schema(description = "The algorithm with which the key should be created.", example = "RSA", allowableValues = {"RSA"})
    @NotNull
    @NotEmpty
    private String algorithm;

    @JacksonXmlProperty(localName = "key_size")
    @Schema(description = "The length of the key. Only applicable for RSA.", example = "2048")
    private Integer keySize;

    @NotEmpty
    @NotNull
    @ArraySchema(arraySchema = @Schema(description = "The password of the P12 container, used for backup and restore"), schema = @Schema(pattern = "^.{1}$"))
    private char[] backupPassword;

    @Schema(description = "The days from today after which the certificate is not valid. e.g. 3650 //valid for 10 years.", example = "3650")
    private Integer validity;

}
