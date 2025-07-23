/**
 * Copyright (c)2025 Securosys SA, authors: Sebastian Fernandez
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class RuleBasedBtcTransactionSigning {

    // --- Helper class to hold a parsed transaction output ---
    public static class TxOut {
        public final long value; // Value in satoshis
        public TxOut(long value) {
            this.value = value;
        }
    }

    public static void main(String[] args) throws IOException {
        //Read json data from TEE as JvmInput object
        // String b64SampleBtcTestNetTransaction = "AgAAAFGQWDzm2FXCCJiyG/rt6B1hMUpDAG7lk32WZT63+T8tO7EwKc57H1We9edH/KxDnxRVoux8Xwm3IpB5XnBmUEQqLjMTFMLqd0tFpahYaQIDFEbs8roEK0HObCSzY9X9ZQAAAAAZdqkUHhLosNkzeb9aGm5/DlziWX0iC8KIrGgpAAAAAAAA/////3YJXKNPF2Ft3QqmzAPlAfM3Wp70fDdeeF+JQxfeLhFJAAAAAAEAAAA=";
        // address: tb1qrcfw3vxexdum7ks6delsuh8zt97jyz7z96ma8a

        String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(input);
        byte[] decoded = Base64.getDecoder().decode(jsonNode.get("input").asText());

        try {
            Task.TaskLevel6 taskLevel = mapper.readValue(decoded, Task.TaskLevel6.class);
            if (taskLevel.getApprovalToBeSigned() == null || taskLevel.getApprovalToBeSigned().isEmpty()) {
                throw new RuntimeException("ApprovalToBeSigned is not found in input");
            }
            String approvalToBeSigned = taskLevel.getApprovalToBeSigned();

            String base64Metadata = taskLevel.getMetaData();
            byte[] metadata = Base64.getDecoder().decode(base64Metadata.getBytes());
            if (metadata == null) {
                throw new RuntimeException("Input is invalid: Metadata is missing or empty.");
            }
            // --- 2. PARSE THE METADATA AND EXTRACT THE TRANSACTION HEX ---
            JsonNode metadataNode = mapper.readTree(metadata);
            JsonNode txHexNode = metadataNode.get("Unsigned Transaction Hex");
            if (txHexNode == null || txHexNode.isNull() || !txHexNode.isTextual()) {
                throw new RuntimeException("Input is invalid: 'Unsigned Transaction Hex' field is missing or not text in metadata.");
            }
            String unsignedTxHex = txHexNode.asText();
            List<TxOut> outputs = parseTransactionOutputs(hexToBytes(unsignedTxHex));
            Optional<TxOut> firstOutput = outputs.stream().findFirst();
            if(!firstOutput.isPresent()) {
                throw new RuntimeException("Full Transaction Input is invalid: no amount to spend declared");
            }

            TxOut amountToSpend = firstOutput.get();
            if (amountToSpend.value > 700L) {
                // Found an output that violates the rule.
                String reason = "REJECTED Extracted Amount (Satoshis) is: " + amountToSpend.value + ", limit exceeds treshhold.";
                System.out.write(reason.getBytes(StandardCharsets.UTF_8));
                System.out.flush();
                System.exit(0);
                return;
            }

            // no violations, sign the challenge
            byte[] decodedApprovalToBeSigned = Base64.getDecoder().decode(approvalToBeSigned);
            System.out.write(decodedApprovalToBeSigned);
            System.out.flush();
            System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses a raw transaction byte array to extract its outputs.
     * This handles both legacy and SegWit format transactions.
     *
     * @param rawTxBytes The raw transaction as a byte array.
     * @return A list of TxOut objects.
     */
    public static List<TxOut> parseTransactionOutputs(byte[] rawTxBytes) {
        ByteBuffer buffer = ByteBuffer.wrap(rawTxBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        List<TxOut> outputs = new ArrayList<>();

        buffer.getInt(); // 1. Skip Version

        // 2. Check for SegWit Marker (0x00, 0x01)
        boolean isSegWit = buffer.get(buffer.position()) == 0x00 && buffer.get(buffer.position() + 1) == 0x01;
        if (isSegWit) {
            buffer.get(); // Consume marker 0x00
            buffer.get(); // Consume flag 0x01
        }

        long vinCount = readVarInt(buffer); // 3. Number of inputs
        // 4. Skip over all inputs
        for (int i = 0; i < vinCount; i++) {
            buffer.position(buffer.position() + 36); // Skip outpoint (32-byte hash + 4-byte index)
            long scriptSigLen = readVarInt(buffer);
            buffer.position(buffer.position() + (int) scriptSigLen); // Skip scriptSig
            buffer.getInt(); // Skip sequence
        }

        long voutCount = readVarInt(buffer); // 5. Number of outputs

        // 6. Parse all outputs
        for (int i = 0; i < voutCount; i++) {
            long value = buffer.getLong();
            long scriptPubKeyLen = readVarInt(buffer);
            // We can skip reading the script itself if we only need the value
            buffer.position(buffer.position() + (int) scriptPubKeyLen);
            outputs.add(new TxOut(value));
        }

        return outputs;
    }

    // Helper to read a Bitcoin-style variable-length integer (VarInt)
    public static int readVarInt(ByteBuffer buffer) {
        byte first = buffer.get();
        int value = first & 0xFF; // Treat as unsigned byte
        if (value < 0xFD) {
            return value;
        } else if (value == 0xFD) {
            return buffer.getShort() & 0xFFFF; // Read 2 bytes as unsigned short
        } else if (value == 0xFE) {
            return buffer.getInt(); // Read 4 bytes as int (up to 2^32-1, fits Java int if positive)
        } else {
            // For bitcoin, VarInts for lengths are usually not 8 bytes,
            // but technically possible. Throw error if we expect smaller lengths.
            // Here, scriptCode length won't be an 8-byte VarInt.
            // For simplicity, assuming it fits int.
            // long val = buffer.getLong(); -> if it could be an 8-byte varint
            throw new IllegalArgumentException("8-byte VarInt for script length not supported/expected here.");
        }
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
