// recipientdao.java
package com.cougararray.RecDatabase;

import java.sql.*;

import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;

public class recipientdao extends Database {
    private final String NON_EXISTENT_NULL = "NA";

    private String  Name;
    private String  Address   = NON_EXISTENT_NULL;
    private int     port      = -1;
    private String  publicKey = NON_EXISTENT_NULL;
    private boolean persistent;

    // --- Constructors ---
    public recipientdao(RecordValue record) {
        super();
        this.persistent = getUser(record.returnStatement());
    }

    public recipientdao(String Address, int port, String publicKey) {
        super();
        this.Address   = Address;
        this.port      = port;
        this.publicKey = publicKey;
    }

    public recipientdao(String Address, int port, String publicKey, String name) {
        super();
        this.Address   = Address;
        this.port      = port;
        this.publicKey = publicKey;
        this.Name      = name;
    }

    // --- Getter / Setter ---
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        if (updateUser("IP_ADDRESS", getAddress(), "NAME", name)) {
            this.Name = name;
        } else {
            Output.print("Error setting name.", Status.BAD);
        }
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        if (updateUser("IP_ADDRESS", getAddress(), "IP_ADDRESS", address)) {
            this.Address = address;
        } else {
            Output.print("Error setting address.", Status.BAD);
        }
    }

    public int getPort() {
        return port;
    }

    public boolean setPort(int newPort) {
        String sql = "UPDATE Users SET PORT = ? WHERE IP_ADDRESS = ?";
        try (Connection          conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt   (1, newPort);
            pstmt.setString(2, getAddress());
            if (pstmt.executeUpdate() == 1) {
                this.port = newPort;
                return true;
            }
        } catch (SQLException e) {
            Output.print("Error updating port: " + e.getMessage(), Status.BAD);
        }
        return false;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        if (updateUser("IP_ADDRESS", getAddress(), "PUBLICKEY", publicKey)) {
            this.publicKey = publicKey;
        } else {
            Output.print("Error setting publicKey.", Status.BAD);
        }
    }

    public boolean exists() {
        return !NON_EXISTENT_NULL.equals(Address) && !NON_EXISTENT_NULL.equals(publicKey);
    }

    // --- Internal helpers ---
    private boolean getUser(String statement) {
        try (Connection          conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(statement);
             ResultSet          rs   = pstmt.executeQuery()) {

            if (rs.next()) {
                this.Address   = rs.getString("IP_ADDRESS");
                this.port      = rs.getInt   ("PORT");
                this.Name      = rs.getString("NAME");
                this.publicKey = rs.getString("PUBLICKEY");
                return true;
            }
        } catch (SQLException e) {
            Output.print("Error retrieving user: " + e.getMessage(), Status.BAD);
        }
        return false;
    }

    private boolean updateUser(String identifier, String identifierValue, String columnToUpdate, String newValue) {
        String sql = "UPDATE Users SET " + columnToUpdate + " = ? WHERE " + identifier + " = ?";
        try (Connection          conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newValue);
            pstmt.setString(2, identifierValue);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                Output.print("No records updated. User not found.", Status.BAD);
                return false;
            }
            return true;
        } catch (SQLException e) {
            Output.print("Error updating user: " + e.getMessage(), Status.BAD);
            return false;
        }
    }

    // --- Public CRUD operations ---
    public boolean createUser() {
        boolean created = createRecord(this.Address, this.port, this.publicKey, this.Name);
        if (created) {
            Output.print("User " + this.getName() + " added.", Status.GOOD);
            return true;
        } else {
            Output.print("Error adding user: " + this.getName(), Status.BAD);
            return false;
        }
    }

    public boolean deleteuser() {
        if (!exists()) {
            Output.print("User does not exist.", Status.BAD);
            return false;
        }
        boolean deleted = deleteRecord(getAddress());
        if (deleted) {
            Output.print("User " + getName() + " deleted.", Status.GOOD);
            return true;
        } else {
            Output.print("Error deleting user: " + getName(), Status.BAD);
            return false;
        }
    }

    public void print() {
        Output.print(
            "Name       - " + getName()      + "\n" +
            "Address    - " + getAddress()   + "\n" +
            "Port       - " + getPort()      + "\n" +
            "PublicKey  - " + getPublicKey() + "\n" +
            "In Database?- " + persistent,
            Status.DASH
        );
    }

    // --- Test Main ---
    public static void main(String[] args) {
        // create and print
        recipientdao user = new recipientdao("127.0.0.1", 8080, "TestKey", "localhost");
        user.createUser();
        user.print();

        // update
        user.setPort(9090);
        user.setPublicKey("TestKey2");
        user.print();

        // lookup
        recipientdao lookup = new recipientdao(new RecordValue(ColumnName.IP_ADDRESS, "127.0.0.1"));
        lookup.print();
    }
}
