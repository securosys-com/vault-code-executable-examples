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
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "CreateKeyAttributes", description = "The attributes of the key that should be created. At least one "
        + "operation (decrypt, sign, unwrap) must be allowed (true).")
public class AttributesDto {

    @Schema(description = "If true the key can be used to encrypt data. This attribute is only supported for symmetric keys.")
    private Boolean encrypt;

    @NotNull
    @Schema(description = "If true the key can be used to decrypt data.")
    private Boolean decrypt;

    @Schema(description = "This attribute is only supported for symmetric keys.")
    private Boolean verify;

    @NotNull
    @Schema(description = "If true the key can sign.")
    private Boolean sign;

    @Schema(description = "If true the key can be used to wrap another key. This attribute is only supported for symmetric keys.")
    private Boolean wrap;

    @NotNull
    @Schema(description = "If true the key can be used to unwrap keys.")
    private Boolean unwrap;

    @Schema(description = "If true it is possible to derive from this key.", defaultValue = "false")
    private boolean derive = false;

    @Schema(description = "If true the key derivation is done using BIP32. This option can only be true if the key's algorithm "
            + "is EC or ED and the derive attribute is true.", defaultValue = "false")
    private boolean bip32 = false;

    @Schema(description = "If true the key is extractable. This option can only be true for keys without smart key attributes.",
            defaultValue = "false")
    private boolean extractable = false;

    @Schema(description = "If true the key can be modified. The 'modifiable' attribute applies exclusively to the key attribute and not to SKA-Policy.", defaultValue = "true")
    private boolean modifiable = true;

    @Schema(description = "If true the key can be deleted.", defaultValue = "false")
    private boolean destroyable = false;

    @Schema(description = "If true the key is sensitive. To export a key sensitive must be false")
    private Boolean sensitive;

    @Schema(description = "If true the encrypted key can be stored in an external memory", defaultValue = "false")
    private boolean copyable = false;
}
