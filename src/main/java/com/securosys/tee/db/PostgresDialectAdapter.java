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
package com.securosys.tee.db;

import com.securosys.tee.dto.DatabaseConnection;

import java.sql.*;

public class PostgresDialectAdapter implements DatabaseAdapter {
   private final DatabaseConnection connection;
   private final DatabaseConnection masterConnection;

    public PostgresDialectAdapter(DatabaseConnection connection, DatabaseConnection masterConnection) {
        this.connection = connection;
        this.masterConnection = masterConnection;
    }

    @Override
    public void saveState(String key, String value) {
        DatabaseConnection temp=connection;
        if(masterConnection!=null){
            temp=masterConnection;
        }
        try (Connection conn = DriverManager.getConnection(temp.getUrl(), temp.getUsername(), temp.getPassword())) {
            String sql = "INSERT INTO " + temp.getTable() + " (\"key\", \"value\") " +
                    "VALUES (?, ?) " +
                    "ON CONFLICT (\"key\") DO UPDATE SET \"value\" = EXCLUDED.value";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, key);
                ps.setString(2, value);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public String readState(String key, String defaultValue) {
        try (Connection conn = DriverManager.getConnection(connection.getUrl(), connection.getUsername(), connection.getPassword())) {
            String sql = "SELECT \"value\" FROM " + connection.getTable() + " WHERE \"key\" = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,key);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("value");
            }
            return defaultValue;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public void deleteState(String key) {
        DatabaseConnection temp=connection;
        if(masterConnection!=null){
            temp=masterConnection;
        }
        try (Connection conn = DriverManager.getConnection(temp.getUrl(), temp.getUsername(), temp.getPassword())) {
            String sql = "DELETE FROM "+connection.getTable()+" WHERE \"key\"= ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,key);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

    }

}
