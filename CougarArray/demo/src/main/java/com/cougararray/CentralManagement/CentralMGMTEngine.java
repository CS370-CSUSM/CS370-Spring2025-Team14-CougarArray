package com.cougararray.CentralManagement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

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
import com.cougararray.RecDatabase.recipientdao;
import com.cougararray.TCPWebsocket.Packets.ClosePacket;
import com.cougararray.TCPWebsocket.Packets.ContentPacket;
import com.cougararray.TCPWebsocket.Packets.ExecutePacket;
import com.cougararray.TCPWebsocket.Packets.ResponsePacket;
import com.cougararray.TCPWebsocket.WebsocketListener;
import com.cougararray.TCPWebsocket.WebsocketSenderClient;

/**
 * CentralMGMTEngine holds backend logic for console
 *
 * Responsibilities:
 * - Handles registration and execution of CLI commands (e.g., encrypt, decrypt, send, adduser).
 * - Manages users via a local database.
 * - Sends and receives encrypted content over WebSocket.
 * - Interfaces with CryptographyClient and various packets for secure comms.
 * - Initializes key-pair if not already valid in configuration.
 *
 * Extends WebsocketListener and sets up a WebSocketServer
 * for handling incoming encrypted packets.
 */
public class CentralMGMTEngine extends WebsocketListener {

    private static final config Config = new config();
    private final Map<String, CommandHandler> commandMap = new HashMap<>();
    private final Map<String, String> commandUsage = new HashMap<>();

    public CentralMGMTEngine() {
        super();
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
        String adduserHelp = "Usage: adduser <address> <name> <publicKey>\nAdds a new user with their IP, public key, and device name.";
        commandUsage.put(adduserCmd, adduserHelp);
        commandMap.put(adduserCmd, params -> {
            if (params.length < 4) {
                return Output.errorPrint(getUsage(adduserCmd));
            }

            String address = params[1];
            String name = params[2];
            String publicKey = params[3];

            if (publicKey.isEmpty() && name.isEmpty()) {
                return Output.errorPrint("Public key and name cannot be empty.");
            }

            if (name.isEmpty())
            {
                return Output.errorPrint("Name cannot be empty.");
            }

            if (publicKey.isEmpty())
            {
                return Output.errorPrint("Public key cannot be empty.");
            }

            return addUser(address, publicKey, name);
        });

        // Deleteuser command
        final String deleteuserCmd = "deleteuser";
        String deleteuserHelp = "Usage: deleteuser <address|name>\nDeletes a user by their IP address or name.";
        commandUsage.put(deleteuserCmd, deleteuserHelp);
        commandMap.put(deleteuserCmd, params -> {
            if (params.length > 1) {
                // Try finding user by address first
                String address = params[1];
                recipientdao user = new recipientdao(new RecordValue(ColumnName.IP_ADDRESS, address));
                
                // If user with given address doesn't exist, try by name
                if (!user.exists()) {
                    user = new recipientdao(new RecordValue(ColumnName.NAME, address));
                }

                if (user.exists()) {
                    return user.deleteuser();
                } else {
                    return Output.errorPrint("User " + params[1] + " not found. Ensure the address or name is correct.");
                }
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
                    String name = params[1];
                    recipientdao user = new recipientdao(new RecordValue(ColumnName.NAME, name));
                    if(user.exists())
                    {
                        return sendFile(params[1], new RecordValue(ColumnName.NAME, params[3]));
                    }
                    else
                    {
                        return Output.errorPrint("User " + name + " does not exist in local users table.");
                    }
                    
                } else if (params[2].toLowerCase().contains("address")) 
                {
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
        
        // ls command
        final String lsCmd = "ls";
        String lsHelp = "Usage: ls\n Lists contents of current directory.";
        commandUsage.put(lsCmd, lsHelp);
        commandMap.put(lsCmd, params -> {
            File currentDir = new File("."); // Current directory
            File[] files = currentDir.listFiles();
    
            if (files != null) {
                Arrays.sort(files); // Sort files alphabetically
                for (File file : files) {
                    Output.print(file.getName(), Status.DASH);
                }
            } else {
                Output.errorPrint("Could not list files in directory.");
            }
            return true;
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
                    Output.print(helpText, Status.DASH);
                } else {
                    Output.errorPrint("No help available for command: " + command);
                }
            } else {
                Output.print("Available commands:", Status.DASH);
                commandUsage.keySet().forEach(cmd -> Output.print("  " + cmd, Status.DASH));
                Output.print("Use 'help <command>' for details on a specific command.", Status.DASH);
            }
            return true;
        });
    }

    /**
     * Returns usage info for a command
     *
     * @param command The command keyword ("encrypt" or "users")
     * @return String containing usage instructions
     */
    private String getUsage(String command) {
        String helpText = commandUsage.get(command);
        if (helpText == null) {
            return "Usage information not available.";
        }
        String[] lines = helpText.split("\n");
        return lines.length > 0 ? lines[0] : "Usage information not available.";
    }

    /**
     * Executes CLI arguments
     *
     * @param parameters Command arguments, index[0] holds the command
     * @return ModalOutput object with success/failure status
     */
    public ModalOutput executeArgs(String[] parameters) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        
        if (parameters.length == 0) {
            Output.printInline("Error: No command provided. Try 'help'", Status.BAD);
            String outputS = baos.toString().trim();
            return new ModalOutput(false, outputS, true);
        }

        String commandKey = parameters[0].toLowerCase();
        CommandHandler handler = commandMap.get(commandKey);

        if (handler == null) {
            Output.printInline("Unknown command! Try 'help'", Status.BAD);
            String outputS = baos.toString().trim();
            return new ModalOutput(false, outputS, true);
        }

        System.setOut(new PrintStream(baos));
        ModalOutput output = new ModalOutput(handler.handle(parameters));
        
        
        String outputS = baos.toString().trim();
        System.setOut(originalOut);
        //System.out.println(outputS); debug purposes
        output.setOutput(outputS);
        return new ModalOutput(true, outputS);
    }

    /**
     * Functional interface for defining CLI behavior
     * Each handler takes string array arguments and returns boolean on success/fail scenarios
     */
    @FunctionalInterface
    private interface CommandHandler {
        boolean handle(String[] parameters) throws IOException;
    }

    /**
     * Encrypts a file using user's key
     * @param file Path to the file to encrypt
     * @return true if encryption is successful, false otherwise
     */
    private boolean encryptFile(String file) {
        return new CryptographyClient(Config.getKeys()).encrypt(file);
    }

    /**
     * Decrypts a file using user's private key
     * @param file Path to the file to decrypt
     * @return true if decryption is successful, false otherwise
     */
    private boolean decryptFile(String file) {
        return new CryptographyClient(Config.getKeys()).decrypt(file);
    }

    /**
     * Encrypts and sends a file to the specified recipient based on IP or name
     *
     * @param file The file path to send
     * @param record The IP (or name) of the recipient
     * @return true if file is sent, false otherwise
     */
    private boolean sendFile(String file, RecordValue record) {
        Output.print("sending file...");
        Output.print(record.returnStatement());
        recipientdao endUser = new recipientdao(record);
        if (!endUser.exists()) return false;
        Output.print("User exists!", Status.GOOD);
        Output.print("publicKey" + endUser.getPublicKey());
        Output.print("filepath" + file);
        CryptographyResult output = CryptographyClient.encrypt(file, endUser.getPublicKey());
        if(!output.successful()) return false;
        ContentPacket packetToBeSent = new ContentPacket(file, output.encryptedData, output.encryptedKey);
        WebsocketSenderClient.sendMessage(endUser.getAddress() + ":5666", packetToBeSent.toJson());

        Output.print("Sent: " + packetToBeSent.toJson());
        return true;
    }

    /**
     * Adds a new user to the database
     *
     * @param address   The IP address of the new user
     * @param publicKey The user's public key
     * @param name      The name of the device/user
     * @return true if the user is successfully added
     */
    private boolean addUser(String address, String publicKey, String name){
        recipientdao newUser = new recipientdao(address, publicKey, name);
        return newUser.createUser();
    }

    /**
     * Lists all registered users
     *
     * @return true if listing is successful
     */
    private boolean listUsers() {
        return new Database().formatPrint();
    }

    /**
     * Starts WebSocket server and listens for incoming packets
     *
     */
    protected void listen(){
        Output.print("Starting WebSocket Receiver on port " + port, Status.GOOD);

        server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onMessage(WebSocket conn, String message) {
                
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
                            ModalOutput status = executeArgs(breakDownArgs(executePacket.getCommand()));
                            conn.send(ResponsePacket.toJson(status.outputStatusToInt(), status.getOutput()));                            
                        default:
                            //Output.print("I didn't find anything!");
                            conn.send(ResponsePacket.toJson(1, "Inappropriate Packet."));
                    }
                } catch (Exception e) {
                    Output.print("Invalidif  JSON: " + e.getMessage());
                }

                //conn.send("Message received: " + message);
                conn.send(ClosePacket.toJson());
                conn.close();
            }

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                Output.print("NEW CONNECTION!");
            }
            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {}
            @Override
            public void onError(WebSocket conn, Exception ex) {}
            @Override
            public void onStart() {}
        };

        server.start();
    }

    /**
     * Breaks command input into valid arguments
     * @param s raw user input
     * @return String[] of valid arguments
     */
    protected static String[] breakDownArgs(String s){
        String[] words = s.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("[^\\w.:]", "");
            //System.err.println(words[i]);
        }
        return words;
    }
}