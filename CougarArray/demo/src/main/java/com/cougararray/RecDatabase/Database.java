// Database.java
package com.cougararray.RecDatabase;

import java.io.File;
import java.sql.*;

import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;

public class Database {
    private static final String DATABASE_FILE = "database.sql";
    protected static final String DATABASE_URL  = "jdbc:sqlite:" + DATABASE_FILE;
    private static boolean       isTableVerified = false;

    public Database() {
        createDatabase();
        createUsersTable();
    }

    private void createDatabase() {
        File dbFile = new File(DATABASE_FILE);
        if (!dbFile.exists()) {
            try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
                if (conn != null) {
                    Output.print("Database created successfully.", Status.GOOD);
                }
            } catch (SQLException e) {
                Output.print("Error creating database: " + e.getMessage(), Status.BAD);
            }
        }
    }

    private void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Users ("
                   + "IP_ADDRESS TEXT NOT NULL, "
                   + "PORT       INTEGER NULL, "
                   + "NAME       TEXT    DEFAULT NULL, "
                   + "PUBLICKEY  TEXT    NOT NULL"
                   + ");";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement  stmt = conn.createStatement()) {
            stmt.execute(sql);
            if (!isTableVerified) {
                Output.print("Users table verified/created successfully.", Status.GOOD);
                isTableVerified = true;
            }
        } catch (SQLException e) {
            Output.print("Error creating Users table: " + e.getMessage(), Status.BAD);
        }
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public boolean formatPrint() {
        String sql = "SELECT IP_ADDRESS, PORT, NAME, PUBLICKEY FROM Users";

        try (Connection    conn = getConnection();
             Statement     stmt = conn.createStatement();
             ResultSet     rs   = stmt.executeQuery(sql)) {

            System.out.printf("%-15s | %-5s | %-15s | %-50s%n",
                              "IP_ADDRESS","PORT","NAME","PUBLICKEY");
            System.out.println("----------------------------------------------------------------------------");

            while (rs.next()) {
                String ipAddress = rs.getString("IP_ADDRESS");
                int    port      = rs.getInt   ("PORT");
                String name      = rs.getString("NAME");
                String pubKey    = rs.getString("PUBLICKEY");

                String shortenedKey = pubKey.length() > 25
                    ? pubKey.substring(0,20) + "..." + pubKey.substring(pubKey.length()-5)
                    : pubKey;

                System.out.printf("%-15s | %-5d | %-15s | %-50s%n",
                                  ipAddress,
                                  port,
                                  name != null ? name : "NULL",
                                  shortenedKey);
            }
            return true;
        } catch (SQLException e) {
            Output.print("Error retrieving Users table: " + e.getMessage(), Status.BAD);
            return false;
        }
    }

    // Legacy insert (no port)
    public void createRecord(String address, String publicKey) {
        String sql = "INSERT INTO Users (IP_ADDRESS, PORT, NAME, PUBLICKEY) VALUES (?, NULL, NULL, ?)";
        try (Connection          conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, address);
            pstmt.setString(2, publicKey);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Output.print("Error creating record: " + e.getMessage(), Status.BAD);
        }
    }

    // Legacy insert (name, no port)
    public boolean createRecord(String address, String publicKey, String name) {
        String sql = "INSERT INTO Users (IP_ADDRESS, PORT, NAME, PUBLICKEY) VALUES (?, NULL, ?, ?)";
        try (Connection          conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, address);
            pstmt.setString(2, name);
            pstmt.setString(3, publicKey);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            Output.print("Error creating record: " + e.getMessage(), Status.BAD);
            return false;
        }
    }

    // New overloads including port
    public void createRecord(String address, int port, String publicKey) {
        String sql = "INSERT INTO Users (IP_ADDRESS, PORT, NAME, PUBLICKEY) VALUES (?, ?, NULL, ?)";
        try (Connection          conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, address);
            pstmt.setInt   (2, port);
            pstmt.setString(3, publicKey);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Output.print("Error creating record: " + e.getMessage(), Status.BAD);
        }
    }

    public boolean createRecord(String address, int port, String publicKey, String name) {
        String sql = "INSERT INTO Users (IP_ADDRESS, PORT, NAME, PUBLICKEY) VALUES (?, ?, ?, ?)";
        try (Connection          conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, address);
            pstmt.setInt   (2, port);
            pstmt.setString(3, name);
            pstmt.setString(4, publicKey);
            return pstmt.executeUpdate() == 1;
        } catch (SQLException e) {
            Output.print("Error creating record: " + e.getMessage(), Status.BAD);
            return false;
        }
    }

    public boolean deleteRecord(String address) {
        String sql = "DELETE FROM Users WHERE IP_ADDRESS = ?";
        try (Connection          conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, address);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Output.print("Error deleting record: " + e.getMessage(), Status.BAD);
            return false;
        }
    }
}
