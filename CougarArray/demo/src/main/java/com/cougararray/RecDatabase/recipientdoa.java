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
        Name = name;
    }
    
    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
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
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
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
    
}