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
package com.securosys.tee.dto.tsb.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "Response containing the OneTimeCode.")
@AllArgsConstructor
@NoArgsConstructor
public class ApproverCreateResponseDto {

    @NotEmpty
    @NotNull
    @ArraySchema(arraySchema = @Schema(description = "The OneTimeCode used for authentication of the onboarding process"), schema = @Schema(pattern = "^.{1}$"))
    private char[] oneTimeCode;

    @Override
    public String toString() {
        return "ApproverCreateResponseDto{" +
                ", oneTimeCode='" + oneTimeCode + '\'' +
                '}';
    }
}
