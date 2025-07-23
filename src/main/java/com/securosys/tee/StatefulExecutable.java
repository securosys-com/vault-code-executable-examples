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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securosys.tee.dto.JvmInput;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

public class StatefulExecutable {
    public static void loadDriver(String jdbcUrl) {
        String driverClassName = getDriverClassName(jdbcUrl);

        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver not found for: " + jdbcUrl + " (expected class: " + driverClassName + ")", e);
        }
    }
    public static int getAndIncrement(String keyToUpdate, String url, String user, String password, String table) {
        loadDriver(url);
        String name=getJdbcName(url);

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false); // begin transaction

            // Use backticks for column names to avoid SQL reserved word conflict
            String selectSQL = "SELECT `value` FROM " + table + " WHERE `key` = ? FOR UPDATE";
            if(name.equals("postgresql")){
                selectSQL = "SELECT \"value\" FROM " + table + " WHERE \"key\" = ? FOR UPDATE";

            }
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
                selectStmt.setString(1, keyToUpdate);
                ResultSet rs = selectStmt.executeQuery();

                int currentValue = 0;
                if (rs.next()) {
                    currentValue = Integer.parseInt(rs.getString("value"));
                } else {
                    String updateSQL ="INSERT INTO " + table + " (`key`, `value`) VALUES (?, ?)";
                    if(name.equals("postgresql")){
                        updateSQL = "INSERT INTO " + table + " (\"key\", \"value\") VALUES (?, ?)";

                    }
                    try (PreparedStatement insertStmt = conn.prepareStatement(updateSQL)) {
                        insertStmt.setString(1, keyToUpdate);
                        insertStmt.setString(2, "0");
                        insertStmt.executeUpdate();
                    }
                }

                int newValue = currentValue + 1;
                String updateSQL = "UPDATE " + table + " SET `value` = ? WHERE `key` = ?";
                if(name.equals("postgresql")){
                    updateSQL =  "UPDATE " + table + " SET \"value\" = ? WHERE \"key\" = ?";
                }
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                    updateStmt.setString(1, String.valueOf(newValue));
                    updateStmt.setString(2, keyToUpdate);
                    updateStmt.executeUpdate();
                }

                conn.commit();
                return newValue;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }finally {
                conn.close();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        JvmInput jvmInput = mapper.readValue(input, JvmInput.class);
        byte[] decoded = Base64.getDecoder().decode(jvmInput.getInput());
        String key = new String(decoded, StandardCharsets.UTF_8);

        int incremented = StatefulExecutable.getAndIncrement(
                key,
                jvmInput.getDatabase().getUrl(),
                jvmInput.getDatabase().getUsername(),
                jvmInput.getDatabase().getPassword(),
                jvmInput.getDatabase().getDefaultTable()
        );

        StringBuilder builder = new StringBuilder();
        builder.append("New value for key <" + key + "> = ").append(incremented);
        System.out.write(builder.toString().getBytes());
        System.out.flush();

        System.exit(0);
    }
    private static String getJdbcName(String jdbcUrl) {
        if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            return "postgresql";
        } else if (jdbcUrl.startsWith("jdbc:mariadb:")) {
            return "mariadb";
        } else if (jdbcUrl.startsWith("jdbc:mysql:")) {
            return "mysql";
        } else if (jdbcUrl.startsWith("jdbc:h2:")) {
            return "h2";
        } else {
            throw new UnsupportedOperationException("Unsupported JDBC URL: " + jdbcUrl);
        }
    }
    private static String getDriverClassName(String jdbcUrl) {
        if (jdbcUrl == null) {
            throw new IllegalArgumentException("JDBC URL cannot be null");
        }

        if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        } else if (jdbcUrl.startsWith("jdbc:mariadb:")) {
            return "org.mariadb.jdbc.Driver";
        } else if (jdbcUrl.startsWith("jdbc:mysql:")) {
            return "com.mysql.cj.jdbc.Driver";
        } else if (jdbcUrl.startsWith("jdbc:h2:")) {
            return "org.h2.Driver";
        } else {
            throw new UnsupportedOperationException("Unsupported JDBC URL: " + jdbcUrl);
        }
    }
}
