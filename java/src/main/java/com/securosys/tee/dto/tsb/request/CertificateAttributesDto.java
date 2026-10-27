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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "CertificateAttributesDto", description = "The standard attributes of X.500 series the key that should be created. At least one "
        + "operation (decrypt, sign, unwrap) must be allowed (true).")
public class CertificateAttributesDto {

    @NotNull
    @NotEmpty
    @Schema(description = "Common Name (e.g. server FQDN or YOUR name) []")
    private String commonName;

    @Schema(description = "Country Name (2 letter code) [CH]")
    private String country;

    @Schema(description = "State or Province Name (full name) [Some-State]")
    private String stateOrProvinceName;

    @Schema(description = "Locality Name (eg, city) []")
    private String locality;

    @Schema(description = "Organization Name (eg, company) [Securosys SA]")
    private String organizationName;

    @Schema(description = "Organizational Unit Name (eg, section) []")
    private String organizationUnitName;

    @Schema(description = "Email Address []")
    private String email;

    @Schema(description = "Title []")
    private String title;

    @Schema(description = "Surname []")
    private String surname;

    @Schema(description = "Given name []")
    private String givenName;

    @Schema(description = "initials []")
    private String initials;

    @Schema(description = "Pseudonym []")
    private String pseudonym;

    @Schema(description = "Generation qualifier [Jr., 3rd, or IV]")
    private String generationQualifier;
}
