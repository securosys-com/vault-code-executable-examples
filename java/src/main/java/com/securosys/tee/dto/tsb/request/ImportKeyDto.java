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
import com.securosys.tee.dto.tsb.PolicyDto;
import com.securosys.tee.validation.tsb.Base64Encoded;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Schema(description = "Import key request.")
public class ImportKeyDto {

    @NotEmpty
    @Schema(description = "The name of the key.")
    private String label;

    @NotEmpty
    @Base64Encoded
    @Schema(description = "The generated seed byte sequence between 128 and 512 bits; 256 bits is advised, from which the key is imported (base 64 encoded). Bits can be represented by a mnemonic phrase to write down.",
            format = "byte")
    private String seed;

    @JacksonXmlProperty(localName = "address_truncated")
    private AddressFormatDto addressFormat;

    @NotEmpty
    @Schema(description = "The oid of the curve used for the EC algorithm.",
            example = "1.3.132.0.10")
    private String curveOid;

    @NotNull
    @Valid
    private AttributesDto attributes;

    @Valid
    private PolicyDto policy;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getCurveOid() {
        return curveOid;
    }

    public void setCurveOid(String curveOid) {
        this.curveOid = curveOid;
    }

    public AttributesDto getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributesDto attributes) {
        this.attributes = attributes;
    }

    public PolicyDto getPolicy() {
        return policy;
    }

    public void setPolicy(PolicyDto policy) {
        this.policy = policy;
    }

    public AddressFormatDto getAddressFormat() {
        return this.addressFormat;
    }

    public void setAddressFormat(AddressFormatDto addressFormat) {
        this.addressFormat = addressFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImportKeyDto that = (ImportKeyDto) o;
        return Objects.equals(label, that.label) &&
                Objects.equals(seed, that.seed) &&
                Objects.equals(curveOid, that.curveOid) &&
                Objects.equals(attributes, that.attributes) &&
                Objects.equals(policy, that.policy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, seed, curveOid, attributes, policy);
    }

    @Override
    public String toString() {
        return "ImportKeyDto{" +
                "label='" + label + '\'' +
                ", seed='" + "*******" + '\'' +
                ", curveOid='" + curveOid + '\'' +
                ", attributes=" + attributes +
                ", policy=" + policy +
                '}';
    }

}
