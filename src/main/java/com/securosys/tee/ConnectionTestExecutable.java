/**
 * Copyright (c)2025 Securosys SA, authors: Tomasz Madej
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ConnectionTestExecutable {
    public static void main(String[] args) throws IOException {
        String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(input);
        String payload = new String(Base64.getDecoder().decode(jsonNode.get("input").asText()));
        System.out.println("Hello " + payload);

        // Test connections
        System.out.println("Testing connection: ");
        testConnection("example.com", 80);
        testConnection("example.com", 443);
        testConnection("securosys.com", 80);

        // Summary output
        System.out.println("Testing example.com:80: " + canConnect("example.com", 80));
        System.out.println("Testing example.com:443: " + canConnect("example.com", 443));
        System.out.println("Testing securosys.com:80: " + canConnect("securosys.com", 80));
    }

    private static void testConnection(String host, int port) {
        if (canConnect(host, port)) {
            System.out.println("Connection successful to " + host + " on port " + port);
        } else {
            System.out.println("Failed to connect to " + host + " on port " + port);
        }
    }

    private static boolean canConnect(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
