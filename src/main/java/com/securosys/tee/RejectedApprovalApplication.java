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
package com.securosys.tee;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securosys.tee.dto.request.Task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RejectedApprovalApplication {

    public static void main(String[] args) throws IOException {
            String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(input);
            byte[] decoded = Base64.getDecoder().decode(jsonNode.get("input").asText());

            try {
                Task.TaskLevel6 taskLevel = objectMapper.readValue(decoded, Task.TaskLevel6.class);
                if (taskLevel.getApprovalToBeSigned() == null || taskLevel.getApprovalToBeSigned().equals("")) {
                    throw new RuntimeException("ApprovalToBeSigned is not found in input");
                }
                byte[] prefix = "REJECTED:".getBytes(StandardCharsets.UTF_8);
                byte[] decode = Base64.getDecoder().decode(taskLevel.getApprovalToBeSigned());

                // Step 3: Concatenate both byte arrays
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(prefix);
                outputStream.write(decode);

                byte[] combined = outputStream.toByteArray();

                System.out.write(combined);
                System.out.flush();

                System.exit(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

}
