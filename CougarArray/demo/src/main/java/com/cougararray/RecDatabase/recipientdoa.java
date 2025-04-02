package com.cougararray.RecDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//Recipient DOA inherits Database;
//This class represents an entity/object of a list of objects from a database
//ex. if we made "Lenny" Recipient then it would get lenny's data
//if my age was manipulated, then it should manipulate the database's data
public class recipientdoa extends Database {

    private final String NON_EXISTANT_NULL = "NA";

    //Private Variables to Class
    private String Name = null; //this is ok to be NULL!
    private String Address = NON_EXISTANT_NULL;
    private String publicKey = NON_EXISTANT_NULL;

    //Getters & Setters
    //TODO! Make setters update the database!
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        if (updateUser("IP_ADDRESS", getAddress(), "NAME", getName())) this.Name = name;
        else return; //TODO! what to do in "ELSE" cases?
    }
    
    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        if (updateUser("PUBLICKEY", getPublicKey(), "ADDRESS", getAddress())) this.Address = address;
        else return; //TODO! what to do in "ELSE" cases?
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        if (updateUser("IP_ADDRESS", getAddress(), "PUBLICKEY", getPublicKey())) this.publicKey = publicKey;
        else return; //TODO! what to do in "ELSE" cases?
    }

    //for it not to exist...all the values would be empty
    public boolean exists() {
        return Address == NON_EXISTANT_NULL && publicKey == NON_EXISTANT_NULL;
    }

    //Constructor
    //Find User via Database
    public recipientdoa(String key, keyType type) {
        super(); //execute Database.java constructor in case we didn't create a database beforehand
        getUser(type.returnStatement(key));
    }

    private void getUser(String statement) {
        //statement should be in this format
        //return "SELECT IP_ADDRESS, NAME, PUBLICKEY FROM Users WHERE " + getkeyType() + " = '" + input + "';";

    
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(statement)) {
             
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                this.Address = rs.getString("IP_ADDRESS");
                this.Name = rs.getString("NAME");
                this.publicKey = rs.getString("PUBLICKEY");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving user: " + e.getMessage());
        }
    }

    //return boolean if successful
    private boolean updateUser(String identifier, String identifierValue, String columnToUpdate, String newValue) {
        String sql = "UPDATE Users SET " + columnToUpdate + " = " + newValue + " WHERE "+ identifier + " = " + identifierValue;
    
        try (Connection conn = getConnection(); // Replace with your actual connection method
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("No records updated. User with IP " + identifier + " not found.");
                return false;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions properly in production
        }

        return false;
    }
    
}