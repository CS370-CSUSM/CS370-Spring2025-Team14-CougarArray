package com.cougararray.RecDatabase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

//TODO!
//Discuss what kind of database to use 

//Subsystem
public class Database {

    private static final String DATABASE_FILE = "database.sql";
    protected static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_FILE;

    public Database() {
        createDatabase();
        createUsersTable();
    }

    private void createDatabase() {
        File dbFile = new File(DATABASE_FILE);
        if (!dbFile.exists()) {
            try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
                if (conn != null) {
                    System.out.println("Database created successfully.");
                }
            } catch (SQLException e) {
                System.out.println("Error creating database: " + e.getMessage());
            }
        }
    }

    private void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Users ("
                    + "IP_ADDRESS TEXT NOT NULL, "
                    + "NAME TEXT DEFAULT NULL, "
                    + "PUBLICKEY TEXT NOT NULL);";

        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Users table verified/created successfully.");
        } catch (SQLException e) {
            System.out.println("Error creating Users table: " + e.getMessage());
        }
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }


    //INSERT RECORDS
    //One if NAME is NULL and one if Name is present
    public void createRecord(String address, String publicKey) {
        String sql = "INSERT INTO Users (IP_ADDRESS, NAME, PUBLICKEY) VALUES (?, NULL, ?)";

        try (Connection conn = getConnection();  // Replace with your actual connection method
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, address);
            pstmt.setString(2, publicKey);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly in production
        }
    }

    public boolean createRecord(String address, String publicKey, String name) {
        String sql = "INSERT INTO Users (IP_ADDRESS, NAME, PUBLICKEY) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);  // Replace with your actual connection method
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, address);
            pstmt.setString(2, name);
            pstmt.setString(3, publicKey);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly in production
        }
        return false;
    }

    /**
     * +getUsers()
     * Access the DB for a list of users, returns necessary info (name, IP, status, etc)
     *
     * +searchUser(userName)
     * Used when running from commandline-- searching whether or not a user exists
     *
     * +getUserIP(userName)
     * Takes username and returns actual IP, used to send the file in the end
     */
}
