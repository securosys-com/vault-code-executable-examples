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
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Objects;

@Schema(description = "Contains the crypto currency for which an address should be created. An address can only be created for "
        + "keys with smart key attributes.")
public class AddressFormatDto {

    @JacksonXmlProperty(isAttribute = true)
    @NotEmpty
    @Schema(description = "The crypto currency for which an address should be created.",
            allowableValues = {"BTC", "ETH", "XLM", "XRP", "IOTA"})
    private String format;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return "AddressTruncated{" +
                "format='" + format + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AddressFormatDto that = (AddressFormatDto) o;
        return Objects.equals(format, that.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(format);
    }
}
