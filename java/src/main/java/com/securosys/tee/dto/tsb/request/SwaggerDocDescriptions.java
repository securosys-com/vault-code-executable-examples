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

public class SwaggerDocDescriptions {

    public static final String SIGNED_APPROVAL =
            "A single signed approval is a base64 encoded value that consists of the following components:\n" +
                    "1. The header containing the total size.\n" +
                    "2. The approvalToBeSigned which is the approval token that was signed by the approval client.\n" +
                    "3. The DER encoded signature of the token done by the approval client.\n" +
                    "4. The DER encoded public key of the approval client.";

}
