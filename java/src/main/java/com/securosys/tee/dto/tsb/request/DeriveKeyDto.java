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

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Derive key request.")
public class DeriveKeyDto {

    @NotEmpty
    @Schema(description = "The name of the master key.")
    private String masterKeyLabel;

    @ArraySchema(arraySchema = @Schema(description = "The password of the master key, if the master key has a password set."), schema = @Schema(pattern = "^.{1}$"))
    private char[] masterKeyPassword;

    @NotEmpty
    @Schema(description = "BIP32 derivation path to be used (without leading /)", example = "1/2/3")
    private String derivationPath;

    private Attributes attributes;

    @Data
    @Schema(name = "DeriveKeyAttributes", description = "The attributes of the derived key. The attributes from the base key "
            + "are fetched and applied if no attributes are specified.")
    public static class Attributes {
        @NotNull
        @Schema(description = "If true the key can be used to decrypt data.")
        private Boolean decrypt;

        @NotNull
        @Schema(description = "If true the key can sign.")
        private Boolean sign;

        @NotNull
        @Schema(description = "If true the key can be used to unwrap keys.")
        private Boolean unwrap;

        @Schema(description = "If true it is possible to derive from this key.", defaultValue = "false")
        private boolean derive = false;

        @Schema(description = "If true the key is extractable. This option can only be true for keys without smart key attributes.",
                defaultValue = "false")
        private boolean extractable = false;

        @Schema(description = "If true the key can be modified. The derived key can not be modifiable if the base key is not "
                + "modifiable", defaultValue = "true")
        private boolean modifiable = true;

        @Schema(description = "If true the key can be deleted.", defaultValue = "false")
        private boolean destroyable = false;

        @Schema(description = "If true the key is sensitive. To export a key sensitive must be false")
        private Boolean sensitive;
    }
}
