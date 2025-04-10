package com.cougararray.RecDatabase;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;

//TODO!
//Discuss what kind of database to use 

//Subsystem
public class Database {

    private static final String DATABASE_FILE = "database.sql";
    protected static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_FILE;

    public Database() {
        if (createDatabase()) createUsersTable();
    }

    private boolean createDatabase() {
        File dbFile = new File(DATABASE_FILE);
        if (!dbFile.exists()) {
            Output.print("Database File doesn't exist...Creating File...", Status.GOOD);
            try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
                if (conn != null) {
                    Output.print("Database File Created.", Status.GOOD);
                    return true;
                }
            } catch (SQLException e) {
                return Output.errorPrint("Database Creation Failure: " + e.getMessage());
            }
        }
        return false;
    }

    private void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Users ("
                    + "IP_ADDRESS TEXT NOT NULL, "
                    + "NAME TEXT DEFAULT NULL, "
                    + "PUBLICKEY TEXT NOT NULL);";

        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            Output.print("Created Tables!", Status.GOOD);
        } catch (SQLException e) {
            Output.errorPrint("Error creating Users table: " + e.getMessage());
        }
    }

    public boolean formatPrint() {
        String sql = "SELECT * FROM Users";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.printf("%-20s | %-20s | %-50s%n", "IP_ADDRESS", "NAME", "PUBLICKEY");
            System.out.println("----------------------------------------------------------------------------");

            while (rs.next()) {
                String ipAddress = rs.getString("IP_ADDRESS");
                String name = rs.getString("NAME");
                String publicKey = rs.getString("PUBLICKEY");

                System.out.printf("%-20s | %-20s | %-50s%n", 
                                  ipAddress, 
                                  name != null ? name : "NULL", 
                                  abbreviatePublicKey(publicKey));
            }
            return true;
        } catch (SQLException e) {
            Output.errorPrint("Error retrieving Users table: " + e.getMessage());
        }

        return false;
    }

    private static String abbreviatePublicKey(String publicKey) {
        if (publicKey == null || publicKey.length() <= 20) {
            return publicKey; // if it's short, just return it as-is
        }
        return publicKey.substring(0, 15) + "..." + publicKey.substring(publicKey.length() - 5);
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
