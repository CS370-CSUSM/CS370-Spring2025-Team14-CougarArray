package com.cougararray.CentralManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.jar.Attributes.Name;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import com.cougararray.Config.config;
import com.cougararray.Cryptography.CryptographyClient;
import com.cougararray.Cryptography.CryptographyResult;
import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;
import com.cougararray.RecDatabase.ColumnName;
import com.cougararray.RecDatabase.Database;
import com.cougararray.RecDatabase.RecordValue;
import com.cougararray.RecDatabase.recipientdoa;
import com.cougararray.TCPWebsocket.WebsocketListener;
import com.cougararray.TCPWebsocket.WebsocketSenderClient;
import com.cougararray.TCPWebsocket.Packets.ContentPacket;

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

    public boolean executeArgs(String[] parameters) throws IOException {
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
            case "adduser": //add user <address>
                System.out.println("Paste their public key =>");
                BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
                String publicKey = r.readLine();
                System.out.println("What is the device's name =>");
                String name = r.readLine();
                return addUser(parameters[1], publicKey, name);
                //if (parameters.length > 3) return addUser(parameters[1], parameters[2], parameters[3]);
                //else if (parameters.length > 3) return addUser(parameters[1], parameters[2], null);
            case "users":
                return listUsers();
            case "send": //Send <file> <option: name/address> <name/address>
                if (parameters.length > 2){
                    Output.print(parameters[2].toLowerCase().contains("name"));
                    if (parameters[2].toLowerCase().contains("name")) return sendFile(parameters[1], new RecordValue(ColumnName.NAME, parameters[3]));
                    else if (parameters[2].toLowerCase().contains("address")) return sendFile(parameters[1], new RecordValue(ColumnName.IP_ADDRESS, parameters[3]));
                }
                break;
            case "ping":
                if (parameters.length > 1) WebsocketSenderClient.sendPing(parameters[1]);
                break;
            case "mykeys":
                Output.print("\n----\nPublic Key: " + Config.getPublicKey() +"\n----\n" +"Private Key (DO NOT SHARE): " + Config.getPrivatekey() +"\n----");
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

    private boolean sendFile(String file, RecordValue record) {
        Output.print("sending file...");
        Output.print(record.returnStatement());
        recipientdoa endUser = new recipientdoa(record);
        if (!endUser.exists()) return false;
        Output.print("User exists!");
        CryptographyResult output = CryptographyClient.encryptWithOutsideKey(file, endUser.getPublicKey());
        if(!output.successful()) return false;
        ContentPacket packetToBeSent = new ContentPacket(file, output.encryptedData);
        WebsocketSenderClient.sendMessage(endUser.getAddress() + ":5666", packetToBeSent.toJson());

        //we can assume that user exist & file was created

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
        Output.print("Starting WebSocket Receiver on port " + port, Status.GOOD);

        server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onMessage(WebSocket conn, String message) {

                Output.print("Received: " + message);

                try {
                    JSONObject json = new JSONObject(message);
                    String type = json.getString("type");  // Assumes a "type" field exists
                    Output.print("Parsed JSON Type: " + type);

                    // Example: If you had a "content" or "fileName" field
                    // String content = json.getString("content");
                    // String fileName = json.getString("fileName");
                    // CryptographyClient.decryptBytes(Base64.getDecoder().decode(content), fileName);
                } catch (Exception e) {
                    Output.print("Invalid JSON: " + e.getMessage());
                }

                //Output.print("Received: " + message);
                conn.send("Message received: " + message);
                //ContentPacket recievedMessage = new ContentPacket(message);
                //CryptographyClient.decryptBytes(recievedMessage.getContentBase64(), recievedMessage.getFileName());
            
            }

            //GENERATED BY org.java_websocket
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                // TODO Auto-generated method stub
                return;
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                // TODO Auto-generated method stub
                return;
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                // TODO Auto-generated method stub
                return;
            }

            @Override
            public void onStart() {
                // TODO Auto-generated method stub
                return;
            }            
        };

        server.start();
        
    }
}
