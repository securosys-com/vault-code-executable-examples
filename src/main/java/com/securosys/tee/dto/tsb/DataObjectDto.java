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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.securosys.tee.validation.tsb.Base64Encoded;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Data object")
public class DataObjectDto {

    @Schema(description = "The name of the data object.")
    @NotEmpty
    private String name;

    @Schema(description = "The value (base64 encoded) of the data object. The maximum size of the value is 64 kilobytes.",
            format = "base64")
    @Base64Encoded
    @NotEmpty
    private String value;

    private Attributes attributes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    // we need to set a different name for this schema as the schema names must be unique and 'Attributes' is already used
    @Schema(description = "The attributes of a data object.", name = "DataObjectAttributes")
    public static class Attributes {
        @Schema(description = "If true the data object is encrypted")
        @JsonProperty("private")
        private Boolean priv;

        @Schema(description = "If true the data object can be modified.")
        private Boolean modifiable;

        @Schema(description = "If true the data object can be deleted.")
        private Boolean destroyable;

        public Boolean getPriv() {
            return priv;
        }

        public void setPriv(Boolean priv) {
            this.priv = priv;
        }

        public Boolean getModifiable() {
            return modifiable;
        }

        public void setModifiable(Boolean modifiable) {
            this.modifiable = modifiable;
        }

        public Boolean getDestroyable() {
            return destroyable;
        }

        public void setDestroyable(Boolean destroyable) {
            this.destroyable = destroyable;
        }
    }
}
