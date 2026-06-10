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
import lombok.Data;

@Data
@Schema(description = "Certificate import request, the key must exist, if a certificate already exists (e.g. self-signed), it will be overridden")
public class CertificateImportDto {
    @NotEmpty
    @Schema(description = "The name of the certificate.")
    private String label;

    @Base64Encoded
    @Schema(description = "Certificate that should be set to the imported key", format = "base64")
    private String certificate;
}
