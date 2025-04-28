package com.cougararray.RecDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;

//Recipient DOA inherits Database;
//This class represents an entity/object of a list of objects from a database
//ex. if we made "Lenny" Recipient then it would get lenny's data
//if my age was manipulated, then it should manipulate the database's data
public class recipientdao extends Database {

    private final String NON_EXISTENT_NULL = "NA";

    //Private Variables to Class
    private String Name = null; //this is ok to be NULL!
    private String Address = NON_EXISTENT_NULL;
    private String publicKey = NON_EXISTENT_NULL;
    private boolean persistent = false; //does the record exist? Default: false unless explictly found @TODO! make use of this?

    //Getters & Setters
    //TODO! Make setters update the database!
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        if (updateUser("IP_ADDRESS", getAddress(), "NAME", getName())) this.Name = name;
        else 
        {
            Output.print("Error setting name.", Status.BAD);
            return;
        } //TODO! what to do in "ELSE" cases?
    }
    
    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        if (updateUser("PUBLICKEY", getPublicKey(), "ADDRESS", getAddress())) this.Address = address;
        else
        {
            Output.print("Error setting address.", Status.BAD);
            return;
        }  //TODO! what to do in "ELSE" cases?
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        if (updateUser("IP_ADDRESS", getAddress(), "PUBLICKEY", getPublicKey())) this.publicKey = publicKey;
        else 
        {
            Output.print("Error setting publicKey.", Status.BAD);
            return;
        } //TODO! what to do in "ELSE" cases?
    }

    //for it not to exist...all the values would be empty
    public boolean exists() {
        return Address != NON_EXISTENT_NULL && publicKey != NON_EXISTENT_NULL;
    }

    //Constructor
    //Find User via Database
    public recipientdao(RecordValue record) {
        super(); //execute Database.java constructor in case we didn't create a database beforehand
        this.persistent = getUser(record.returnStatement());
    }

    //Constructor
    //Create User
    public recipientdao(String Address, String publicKey) {
        super();
        this.Address = Address;
        this.publicKey = publicKey;
    }
    public recipientdao(String Address, String publicKey, String name) {
        super();
        this.Address = Address;
        this.publicKey = publicKey;
        this.Name = name;
    }

    private boolean getUser(String statement) {
        //statement should be in this format
        //SELECT IP_ADDRESS, NAME, PUBLICKEY FROM Users WHERE NAME = 'Localhost';

    
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(statement)) {
             
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                this.Address = rs.getString("IP_ADDRESS");
                this.Name = rs.getString("NAME");
                this.publicKey = rs.getString("PUBLICKEY");
                return true;
            }
        } catch (SQLException e) {
            Output.print("Error retrieving user: " + e.getMessage(), Status.BAD);
        }
        return false;
    }

    //return boolean if successful
    private boolean updateUser(String identifier, String identifierValue, String columnToUpdate, String newValue) {

        //ideally, this is an awful way of doing stuff.
        String sql = "UPDATE Users SET " + columnToUpdate + " = \"" + newValue + "\" WHERE "+ identifier + " = \"" + identifierValue + "\"";
        //System.out.println(sql);
    
        try (Connection conn = getConnection(); // Replace with your actual connection method
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                Output.print("No records updated. User with IP " + identifier + " not found.", Status.BAD);
                return false;
            }
            return true;
        } catch (SQLException e) {
            Output.print("Error updating user:" + e.getMessage(), Status.BAD); // Handle exceptions properly in production
        }

        return false;
    }

    public boolean createUser() {
        boolean created = this.createRecord(this.Address, this.publicKey, this.Name);
        if (created) {
            Output.print("User " + this.getName() + " added.", Status.GOOD);
            return true;
        }
        Output.print("Error adding user: " + this.getName(), Status.BAD);
        return false;
    }

    public boolean deleteuser() {
        boolean temp = this.exists();
        if (temp) {
            boolean deleted = this.deleteRecord(this.getAddress());
            if (deleted) {
                Output.print("User " + this.getName() + " deleted.", Status.GOOD);
                return true;
            }
        } else {
            Output.print("User does not exist.", Status.BAD);
        }
        Output.print("Error deleting user: " + this.getName(), Status.BAD);
        return false;
    }

    public void print() {
        Output.print("Name - " + this.getName() + "\n" + "Address - " + this.getAddress() + "\n" + "Public Key - " + this.getPublicKey() + "\n" + "In Database? - " + this.persistent, Status.DASH);
    }

    //TEST
    //Create .sql Database File
    //Create new user; Name: localhost, IP: 127.0.0.1, PublicKey: "Test"
    //Access User by constructor via IP (127.0.0.1)
    //Check if Empty
    //Update PublicKey to "Test2"
    //Output new PublicKey
    //Revert it back
    public static void main(String[] args) {
        //recipientdao localhost = new recipientdao("127.0.0.1", "Test", "localhost");
        //localhost.print();
        //System.err.println("-----------");
        //localhost.createUser();
        recipientdao localhost = new recipientdao(new RecordValue(ColumnName.IP_ADDRESS, "127.0.0.1"));
        localhost.print();
        Output.print(localhost.exists());
        //localhost.setPublicKey("Test2");
        //System.err.println("-----------");
        //localhost.print();

        //localhost.setPublicKey("Test");
    }
    
}