package com.cougararray.CentralManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
import com.cougararray.TCPWebsocket.Packets.ClosePacket;
import com.cougararray.TCPWebsocket.Packets.ContentPacket;
import com.cougararray.TCPWebsocket.Packets.ExecutePacket;
import com.cougararray.TCPWebsocket.Packets.ResponsePacket;
import com.cougararray.TCPWebsocket.WebsocketListener;
import com.cougararray.TCPWebsocket.WebsocketSenderClient;

public class CentralMGMTEngine extends WebsocketListener {

    private static final config Config = new config();
    private final Map<String, CommandHandler> commandMap = new HashMap<>();
    private final Map<String, String> commandUsage = new HashMap<>();

    public CentralMGMTEngine() {
        super(Config.getPort());
        this.start(); 

        if (Config.emptyOrInvalidKeys()) {
            Output.print("Keys for Config are Invalid...Updating Keys");
            if (Config.setKeys(CryptographyClient.generateKeys())) {
                Output.print("Successfully generated keys", Status.GOOD);
            } else {
                Output.errorPrint("Godammit. How did you get here?");
            }
        }
        
        initializeCommandMap();
    }

    private void initializeCommandMap() {

        // Encrypt command
        final String encryptCmd = "encrypt";
        String encryptHelp = "Usage: encrypt <filePath> (LOCAL USAGE)\nEncrypts a local file using the current user's public key.";
        commandUsage.put(encryptCmd, encryptHelp);
        commandMap.put(encryptCmd, params -> {
            if (params.length > 1) return encryptFile(params[1]);
            return Output.errorPrint(getUsage(encryptCmd));
        });

        // Decrypt command
        final String decryptCmd = "decrypt";
        String decryptHelp = "Usage: decrypt <filePath> (LOCAL USAGE)\nDecrypts a local file using the current user's private key.";
        commandUsage.put(decryptCmd, decryptHelp);
        commandMap.put(decryptCmd, params -> {
            if (params.length > 1) return decryptFile(params[1]);
            return Output.errorPrint(getUsage(decryptCmd));
        });

        // Adduser command
        final String adduserCmd = "adduser";
        String adduserHelp = "Usage: adduser <address>\nAdds a new user by specifying their IP address. You will be prompted to input their public key and device name.";
        commandUsage.put(adduserCmd, adduserHelp);
        commandMap.put(adduserCmd, params -> {
            if (params.length > 1) {
                System.out.println("Paste their public key =>");
                BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
                String publicKey = r.readLine();
                System.out.println("What is the device's name =>");
                String name = r.readLine();
                return addUser(params[1], publicKey, name);
            }
            return Output.errorPrint(getUsage(adduserCmd));
        });

        // Deleteuser command
        final String deleteuserCmd = "deleteuser";
        String deleteuserHelp = "Usage: deleteuser <address>\nDeletes a user by their IP address.";
        commandUsage.put(deleteuserCmd, deleteuserHelp);
        commandMap.put(deleteuserCmd, params -> { 
            if (params.length > 1) {
                recipientdoa newUser = new recipientdoa(new RecordValue(ColumnName.IP_ADDRESS, params[1]));
                return newUser.deleteuser();
            }
            return Output.errorPrint(getUsage(deleteuserCmd));
        });

        // Users command
        final String usersCmd = "users";
        String usersHelp = "Usage: users\nLists all users in the database.";
        commandUsage.put(usersCmd, usersHelp);
        commandMap.put(usersCmd, params -> listUsers());

        // Send command
        final String sendCmd = "send";
        String sendHelp = "Usage: send <filePath> <\"name\"/\"address\"> <Name/Address of Device>\nExample: send private.txt name Lenny\nSends an encrypted file to the specified user by their name or IP address.";
        commandUsage.put(sendCmd, sendHelp);
        commandMap.put(sendCmd, params -> {
            if (params.length > 3) {
                if (params[2].toLowerCase().contains("name")) {
                    return sendFile(params[1], new RecordValue(ColumnName.NAME, params[3]));
                } else if (params[2].toLowerCase().contains("address")) {
                    return sendFile(params[1], new RecordValue(ColumnName.IP_ADDRESS, params[3]));
                }
                return Output.errorPrint("Error: Invalid option for send command. Use 'name' or 'address'.");
            }
            return Output.errorPrint(getUsage(sendCmd));
        });

        // Ping command
        final String pingCmd = "ping";
        String pingHelp = "Usage: ping <address>\nSends a ping to the specified address to check connectivity.";
        commandUsage.put(pingCmd, pingHelp);
        commandMap.put(pingCmd, params -> {
            if (params.length > 1) {
                WebsocketSenderClient.sendPing(params[1]);
                return true;
            }
            return Output.errorPrint(getUsage(pingCmd));
        });

        // Mykeys command
        final String mykeysCmd = "mykeys";
        String mykeysHelp = "Usage: mykeys\nDisplays the current user's public and private keys.";
        commandUsage.put(mykeysCmd, mykeysHelp);
        commandMap.put(mykeysCmd, params -> {
            Output.print("\n----\nPublic Key: " + Config.getPublicKey() + "\n----\n" +
                       "Private Key (DO NOT SHARE): " + Config.getPrivatekey() + "\n----");
            return true;
        });

        // Help command
        final String helpCmd = "help";
        String helpHelp = "Usage: help [command]\nDisplays help information. If a command is specified, shows detailed usage for that command.";
        commandUsage.put(helpCmd, helpHelp);
        commandMap.put(helpCmd, params -> {
            if (params.length > 1) {
                String command = params[1].toLowerCase();
                String helpText = commandUsage.get(command);
                if (helpText != null) {
                    Output.print(helpText);
                } else {
                    Output.errorPrint("No help available for command: " + command);
                }
            } else {
                Output.print("Available commands:");
                commandUsage.keySet().forEach(cmd -> Output.print("  " + cmd));
                Output.print("Use 'help <command>' for details on a specific command.");
            }
            return true;
        });
    }

    private String getUsage(String command) {
        String helpText = commandUsage.get(command);
        if (helpText == null) {
            return "Usage information not available.";
        }
        String[] lines = helpText.split("\n");
        return lines.length > 0 ? lines[0] : "Usage information not available.";
    }

    public boolean executeArgs(String[] parameters) throws IOException {
        if (parameters.length == 0) {
            return Output.errorPrint("Error: No command provided");
        }

        String commandKey = parameters[0].toLowerCase();
        CommandHandler handler = commandMap.get(commandKey);

        if (handler == null) {
            return Output.errorPrint("Unknown Command!");
        }

        return handler.handle(parameters);
    }

    @FunctionalInterface
    private interface CommandHandler {
        boolean handle(String[] parameters) throws IOException;
    }

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
        Output.print("publicKey" + endUser.getPublicKey());
        Output.print("filepath" + file);
        CryptographyResult output = CryptographyClient.encrypt(file, endUser.getPublicKey());
        if(!output.successful()) return false;
        ContentPacket packetToBeSent = new ContentPacket(file, output.encryptedData, output.encryptedKey);
        WebsocketSenderClient.sendMessage(endUser.getAddress() + ":5666", packetToBeSent.toJson());

        Output.print("Sent: " + packetToBeSent.toJson());
        return false;
    }

    private boolean addUser(String address, String publicKey, String name){
        recipientdoa newUser = new recipientdoa(address, publicKey, name);
        return newUser.createUser();
    }

    private boolean listUsers() {
        return new Database().formatPrint();
    }

    protected void listen(){
        Output.print("Starting WebSocket Receiver on port " + port, Status.GOOD);

        server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onMessage(WebSocket conn, String message) {
                //Output.print("Received: " + message);
                
                try {
                    JSONObject json = new JSONObject(message);
                    String type = json.getString("type");
                    Output.print("Parsed JSON Type: " + type);

                    switch (type.replaceAll("\\s+", "")) {
                        case "PING":
                            conn.send("PONG!"); //@TODO! make a pong packet
                            break;
                        case "CONTENT":
                            ContentPacket receivePacket = new ContentPacket(message);
                            if (CryptographyClient.decryptBytes(
                                Base64.getDecoder().decode(json.getString("content")),
                                receivePacket.getFileName(),
                                Config.getPrivatekey(),
                                Base64.getDecoder().decode(json.getString("key"))
                            )) conn.send(ResponsePacket.toJson(0));
                            break;
                        case "EXECUTE":
                            ExecutePacket executePacket = new ExecutePacket(message);
                            boolean status = executeArgs(breakDownArgs(executePacket.getCommand()));
                            if (status) conn.send(ResponsePacket.toJson(0));
                            else conn.send(ResponsePacket.toJson(1));
                        default:
                            //Output.print("I didn't find anything!");
                            conn.send(ResponsePacket.toJson(1, "Inappropriate Packet."));
                    }
                } catch (Exception e) {
                    Output.print("Invalid JSON: " + e.getMessage());
                }

                //conn.send("Message received: " + message);
                conn.send(ClosePacket.toJson());
                conn.close();
            }

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {}
            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {}
            @Override
            public void onError(WebSocket conn, Exception ex) {}
            @Override
            public void onStart() {}
        };

        server.start();
    }

    protected static String[] breakDownArgs(String s){
        String[] words = s.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("[^\\w.:]", "");
            //System.err.println(words[i]);
        }
        return words;
    }
}