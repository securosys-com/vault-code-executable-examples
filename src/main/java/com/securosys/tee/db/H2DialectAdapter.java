package com.securosys.tee.db;

import com.securosys.tee.dto.DatabaseConnection;

import java.sql.*;

public class H2DialectAdapter implements DatabaseAdapter {
    private final DatabaseConnection connection;

    public H2DialectAdapter(DatabaseConnection connection) {
        this.connection = connection;
    }

    @Override
    public void saveState(String key, String value) {
        try (Connection conn = DriverManager.getConnection(connection.getUrl(), connection.getUsername(), connection.getPassword())) {
            String sql = "MERGE INTO "+connection.getTable()+" (`key`, `value`) KEY (`key`) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,key);
            ps.setString(2,value);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public String readState(String key, String defaultValue) {
        try (Connection conn = DriverManager.getConnection(connection.getUrl(), connection.getUsername(), connection.getPassword())) {
            String sql = "SELECT `value` FROM " + connection.getTable() + " WHERE `key` = ? FOR UPDATE";
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
        try (Connection conn = DriverManager.getConnection(connection.getUrl(), connection.getUsername(), connection.getPassword())) {
            String sql = "DELETE FROM "+connection.getTable()+" WHERE `key`= ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,key);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

    }
}
