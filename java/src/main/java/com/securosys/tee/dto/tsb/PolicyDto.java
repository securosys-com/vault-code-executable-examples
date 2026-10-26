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
package com.securosys.tee.dto.tsb;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "The policy of a key. Only Asymmetric keys can have a policy (EC, ED, RSA,...). It contains the rules to use this key for signing a payload in a sign request, the "
        + "rules to block and unblock this key, and the rules to modify the policy of this key. If a rule is empty the "
        + "associated operation can be performed without any approvals. If the policy is empty the key does not use smart key "
        + "attributes and it is not possible to add them later. If a policy is used with the key, the key cannot be exported.", nullable = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PolicyDto extends ModifyPolicyDto {

    @JacksonXmlProperty(localName = "key_status")
    @NotNull
    private KeyStatus keyStatus;

    public KeyStatus getKeyStatus() {
        return keyStatus;
    }

    public void setKeyStatus(KeyStatus keyStatus) {
        this.keyStatus = keyStatus;
    }

    @Schema(description = "The status of a key.")
    @Data
    public static class KeyStatus {

        @NotNull
        @Schema(description = "If true the key is blocked.", example = "false")
        private Boolean blocked;

    }
}
