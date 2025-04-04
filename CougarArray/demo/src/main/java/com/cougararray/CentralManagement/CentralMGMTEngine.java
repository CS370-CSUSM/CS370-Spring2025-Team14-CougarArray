package com.cougararray.CentralManagement;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.cougararray.Config.config;
import com.cougararray.Cryptography.CryptographyClient;
import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;
import com.cougararray.RecDatabase.Database;
import com.cougararray.RecDatabase.recipientdoa;
import com.cougararray.TCPWebsocket.WebsocketListener;

//subsystem
//This acts as an event trigger; this parsers then executes the model
public class CentralMGMTEngine extends WebsocketListener {

    private static config Config = new config();

    public CentralMGMTEngine() {
        super(Config.getPort());
        this.start(); //start Websocket

        if (Config.emptyOrInvalidKeys()) {
            Output.print("Keys for Config are Invalid...Updating Keys");
            if (Config.setKeys(CryptographyClient.generateKeys())) {
                Output.print("Successfully generated keys", Status.GOOD);
            } else {
                Output.errorPrint("Godammit. How did you get here?");
            }
        }
        
    }

    public boolean executeArgs(String[] parameters) {
        //I feel like this line of code doesn't make sense...
        //If Java Command can get one line of string then this wouldn't be needed?
        //if (parameters.length == 0) return Output.errorPrint("Error: No command provided");

        switch(parameters[0].toLowerCase()) {
            case "encrypt":
                //safer way of checking length
                if (parameters.length > 1) return encryptFile(parameters[1]);
                break;
            case "decrypt": //@TODO!
                if (parameters.length > 1) return decryptFile(parameters[1]);
                break;
            case "adduser":
                if (parameters.length > 3) return addUser(parameters[1], parameters[2], parameters[3]);
                else if (parameters.length > 3) return addUser(parameters[1], parameters[2], null);
            case "users":
                return listUsers();
            case "send": //@TODO!
                break;
            default:
                return Output.errorPrint("Unknown Command!");
        }

        return false;
    }


    //encryptFile
    //make <<use>> of Encryption.java
    //Example Execution
    //  encrypt file.txt
    //  encrypt full/fill/path/to/file.txt
    //  (parameter[1] is file)
    //Expected Outcome:
    //It should generate an encrypted version of the file
    private boolean encryptFile(String file) {
        return new CryptographyClient(Config.getKeys()).encrypt(file);
    }

    private boolean decryptFile(String file) {
        return new CryptographyClient(Config.getKeys()).decrypt(file);
    }

    private boolean sendFile(String[] parameters) {
        return false;
    }

    private boolean addUser(String address, String publicKey, String name){
        recipientdoa newUser = new recipientdoa(address, publicKey, name);
        return newUser.createUser();
    }

    private boolean listUsers() {
        return new Database().formatPrint();
    }

    //WEBSOCKET INHERITANCE
    //There is a good reason why CentralMGMT inherits WebsocketListener instead of <<uses>> it
    //The reason being is that if the websocket gets data...it should to execute decryption & other functions
    //HOWEVER, it cannot do that because we are using a layered architecture where central execution is done at CentralMGMT subsystem
    //by doing inheritance...CentralMGMT can directly work with the data
    protected void listen(){
        Output.print("Starting Websocket Receiver");

        //Connect to the port
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            Output.print("Server listening on port " + this.port + "...", Status.GOOD);

            //after successful connection, start listening for input messages
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Output.print("Client connected: " + clientSocket.getInetAddress(), Status.GOOD);

                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //this handles messages that are INPUTTED (ex. input would receive information. If I send "Hello World" to a server, the server would save it here)
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true); //this handles what to write back (ex. if I receive "Hello World", output would write back "How are you doing?")
                
                String received;
                while ((received = input.readLine()) != null) { //save input to the strong Received. If there are several lines on the strong; keep saving it until it's empty. (Similiar to reading a file with C++)
                    //TODO! WHAT TO DO WITH DATA HERE?
                }
            }
        } catch (Exception e) {
            Output.print("Error Caught! Error revolves in Websocket.java!", Status.BAD);
            e.printStackTrace();
        }

        Output.print("Leaving Websocket Receiver...");
        
    }
}
