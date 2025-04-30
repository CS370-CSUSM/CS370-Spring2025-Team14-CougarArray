package com.cougararray.CentralManagement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
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
import com.cougararray.Cryptography.FileHasher;
import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;
import com.cougararray.RecDatabase.ColumnName;
import com.cougararray.RecDatabase.Database;
import com.cougararray.RecDatabase.RecordValue;
import com.cougararray.RecDatabase.recipientdao;
import com.cougararray.TCPWebsocket.WebsocketListener;
import com.cougararray.TCPWebsocket.WebsocketSenderClient;
import com.cougararray.TCPWebsocket.Packets.ClosePacket;
import com.cougararray.TCPWebsocket.Packets.ContentPacket;
import com.cougararray.TCPWebsocket.Packets.ExecutePacket;
import com.cougararray.TCPWebsocket.Packets.ResponsePacket;

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
        Output.print("[CentralMGMTEngine.<init>]: Starting constructor", Status.DEBUG);

        if (Config.emptyOrInvalidKeys()) {
            Output.print("Keys for Config are Invalid... Updating Keys", Status.OK);
            if (Config.setKeys(CryptographyClient.generateKeys())) {
                Output.print("Successfully generated keys", Status.GOOD);
            } else {
                Output.errorPrint("Godammit. How did you get here?");
            }
        }

        String configPort = Config.getPort();
        if (configPort != null && !configPort.isEmpty()) {
            try {
                this.port = Integer.parseInt(configPort);
            } catch (NumberFormatException e) {
                Output.print("Invalid port in config, using default 5666", Status.BAD);
                this.port = 5666;
            }
        } else {
            this.port = 5666; // Default if not set
            Output.print("[CentralMGMTEngine.<init>]: Using port " + this.port, Status.DEBUG);
        }
    
        
        initializeCommandMap();
    }


    private void initializeCommandMap() {

        Output.print("[CentralMGMTEngine.initializeCommandMap]: Initializing CLI command mappings", Status.DEBUG);


        final String configCmd = "config";
        String configHelp = "Usage: config\nLists the current program configuration";
        commandUsage.put(configCmd, configHelp);
        commandMap.put(configCmd, params -> {
    
            String port = Config.getPort();
            Output.print("Port: " + (port != null ? port : "Not set (default: 5666)"), Status.DASH);
        
            String debugMode = Config.getDebug();
            Output.print("Debug Mode: " + (debugMode != null ? debugMode : "Not set"), Status.DASH);
            
            String actAsSender = Config.getActAsSender();
            Output.print("Act as Sender: " + (actAsSender != null ? actAsSender : "Not set"), Status.DASH);
            
            String actAsReceiver = Config.getActAsReceiver();
            Output.print("Act as Receiver: " + (actAsReceiver != null ? actAsReceiver : "Not set"), Status.DASH);

            String decryptedPrefix = Config.getDecryptedPrefix();
            Output.print("Decrypted Prefix: " + (decryptedPrefix != null ? decryptedPrefix : "Not set"), Status.DASH);

            String encryptedSuffix = Config.getEncryptedSuffix();
            Output.print("Encrypted Suffix: " + (encryptedSuffix != null ? encryptedSuffix : "Not set"), Status.DASH);
            return true;
        });
        
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
        String adduserHelp = "Usage: adduser <address> [port] <name> <publicKey>\nIf port is omitted or blank, defaults to 5666.";
        commandUsage.put(adduserCmd, adduserHelp);
    
        commandMap.put(adduserCmd, params -> {
            // Either 4 args (no port) or 5 args (with port)
            if (params.length != 4 && params.length != 5) {
                return Output.errorPrint(getUsage(adduserCmd));
            }
    
            String address    = params[1];
            String portString = (params.length == 5) ? params[2] : "";
            String name       = (params.length == 5) ? params[3] : params[2];
            String publicKey  = (params.length == 5) ? params[4] : params[3];
    
            if (name.isEmpty())      return Output.errorPrint("Name cannot be empty.");
            if (publicKey.isEmpty()) return Output.errorPrint("Public key cannot be empty.");
    
            return addUser(address, portString, name, publicKey);
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
        String sendHelp = "Usage: send <filePath> <\"name\"/\"address\"> <Name/Address>\nSends an encrypted file to the specified user.";
        commandUsage.put(sendCmd, sendHelp);
        commandMap.put(sendCmd, params -> {
            if (!"true".equalsIgnoreCase(Config.getActAsSender())) {
                return Output.errorPrint("ActAsSender is disabled in config.");
            }
            if (params.length > 3) {
                RecordValue rv;
                if (params[2].equalsIgnoreCase("name")) {
                    rv = new RecordValue(ColumnName.NAME, params[3]);
                } else if (params[2].equalsIgnoreCase("address")) {
                    rv = new RecordValue(ColumnName.IP_ADDRESS, params[3]);
                } else {
                    return Output.errorPrint("Error: Invalid option for send command. Use 'name' or 'address'.");
                }
                return sendFile(params[1], rv);
            }
            return Output.errorPrint(getUsage(sendCmd));
        });

        // Ping command
        final String pingCmd = "ping";
        String pingHelp = "Usage: ping <address> [port]\nSends a ping to the specified address to check connectivity.";
        commandUsage.put(pingCmd, pingHelp);
        commandMap.put(pingCmd, params -> {
            if(params.length == 2)
            {
                WebsocketSenderClient.sendPing(params[1]);
                return true;
            }
            if(params.length == 3)
            {
                WebsocketSenderClient.sendPing(params[1] + params[2]);
            }
            return Output.errorPrint(getUsage(pingCmd));
        });
        
        // ls command
        final String lsCmd = "ls";
        String lsHelp = "Usage: ls\nLists contents of current directory.";
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
            Output.print("AES Key: " + Config.getAESKey());
            Output.print("Public Key: " + Config.getPublicKey() + "\n" + "Private Key (DO NOT SHARE): " + Config.getPrivateKey() + "\n");
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
                    Output.print("[CentralMGMTEngine.executeArgs]: Showing help for command: " + command, Status.DEBUG);
                    Output.print(helpText, Status.DASH);
                } else {
                    Output.print("[CentralMGMTEngine.executeArgs]: No help entry found for command: " + command, Status.DEBUG);
                    Output.errorPrint("No help available for command: " + command);
                }
            } else {
                Output.print("Available commands:", Status.DASH);
                commandUsage.keySet().forEach(cmd -> Output.print("  " + cmd, Status.DASH));
                Output.print("Use 'help <command>' for details on a specific command.", Status.DASH);
            }
            return true;
        });

        // about command
        final String aboutCmd = "about";
        String aboutHelp = "Usage: about\nDisplays information about the program.";
        commandUsage.put(aboutCmd, aboutHelp);
        commandMap.put(aboutCmd, params -> {
            Output.print("CougarArray allows you to encrypt, decrypt, send, and receive files over your local network & internet!", Status.DASH);
            // Output.print("You may also navigate to the README at https://github.com/CS370-CSUSM/CS370-Spring2025-Team14-CougarArray", Status.DASH);
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

        Output.print("[CentralMGMTEngine.executeArgs]: Received parameters: " + Arrays.toString(parameters), Status.DEBUG);
        
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
        
            Output.print("[CentralMGMTEngine.executeArgs]: Found handler for command: " + commandKey, Status.DEBUG);
        
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
            Output.print("[CentralMGMTEngine.encryptFile]: invoked with file: " + file, Status.DEBUG);
        return new CryptographyClient(Config.getKeys()).encrypt(file);
    }

    /**
     * Decrypts a file using user's private key
     * @param file Path to the file to decrypt
     * @return true if decryption is successful, false otherwise
     */
    private boolean decryptFile(String file) {
            Output.print("[CentralMGMTEngine.decryptFile] invoked with file: " + file, Status.DEBUG);
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
    recipientdao endUser = new recipientdao(record);
    if (!endUser.exists()) return false;

    CryptographyResult cry = CryptographyClient.encrypt(file, endUser.getPublicKey());
    try {
        String encryptedHash = FileHasher.hashBytes(cry.encryptedData);
        ContentPacket packet = new ContentPacket(file, cry.encryptedData, cry.encryptedKey, encryptedHash);
        String target = endUser.getAddress() + ":" + endUser.getPort();
        WebsocketSenderClient.sendMessage(target, packet.toJson());
        return true;
    } catch (Exception e) {
        Output.errorPrint("Error hashing encrypted data: " + e.getMessage());
        return false;
    }
}

    private boolean addUser(String address, String portString, String name, String publicKey) {
        int port = 5666; // default

            Output.print("[CentralMGMTEngine.addUser] called with address: " + address + ", port: " + portString + ", name: " + name + ", key: " + publicKey, Status.DEBUG);
        
        
        if (portString != null && !portString.isEmpty()) {
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                Output.print( "Invalid port '" + portString + "', using default 5666", Status.BAD);
            }
        }
        recipientdao newUser = new recipientdao(address, port, publicKey, name);
        return newUser.createUser();
    }
    /**
     * Lists all registered users
     *
     * @return true if listing is successful
     */
    private boolean listUsers() {
            Output.print("[CentralMGMTEngine.listUsers] called", Status.DEBUG);
        
        return new Database().formatPrint();
    }

    /**
     * Starts WebSocket server and listens for incoming packets
     *
     */
    protected void listen(){
        String actAsReceiverConfig = Config.getActAsReceiver();
        if (actAsReceiverConfig == null || !actAsReceiverConfig.equalsIgnoreCase("true")) {
            Output.print("WebSocket server (receiver) is disabled in config.", Status.OK);
            return;
        }
    
        Output.print("Starting WebSocket Receiver on port " + port, Status.GOOD);

        Output.print("[CentralMGMTEngine.listen]: WebSocket attempting to start...", Status.DEBUG);
        

        server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onMessage(WebSocket conn, String message) {
                Output.print("[CentralMGMTEngine.listen]: Received message - " + message, Status.DEBUG);
                try {
                    JSONObject json = new JSONObject(message);
                    String type = json.getString("type");
                    Output.print("Parsed JSON Type: " + type);
                    Output.print("[CentralMGMTEngine.listen] Parsed JSON Type: " + type, Status.DEBUG);


                    switch (type.replaceAll("\\s+", "")) {
                        case "PING":
                            Output.print("[CentralMGMTEngine.listen] Received PING packet, sending PONG...", Status.DEBUG);
                            conn.send("PONG!"); //@TODO! make a pong packet
                            break;
                        case "CONTENT":
                        ContentPacket receivePacket = new ContentPacket(message);
                        byte[] encryptedContent = Base64.getDecoder().decode(json.getString("content"));
                        String receivedHash = json.getString("hash");

                        try {
                            String computedHash = FileHasher.hashBytes(encryptedContent);
                            if (!computedHash.equals(receivedHash)) {
                                Output.errorPrint("Encrypted data corrupted during transfer.");
                                conn.send(ResponsePacket.toJson(2, "Data corrupted"));
                                break;
                            }
                        } catch (Exception e) {
                            Output.errorPrint("Hash verification failed: " + e.getMessage());
                            conn.send(ResponsePacket.toJson(2, "Hash error"));
                            break;
                        }

                        if (CryptographyClient.decryptBytes(encryptedContent, receivePacket.getFileName(), 
                                Config.getPrivateKey(), Base64.getDecoder().decode(json.getString("key")))) {
                            // Optional: Log decrypted file hash
                            try {
                                byte[] decryptedData = Files.readAllBytes(Paths.get(receivePacket.getFileName()));
                                String decryptedHash = FileHasher.hashBytes(decryptedData);
                                Output.print("Decrypted file hash: " + decryptedHash, Status.DEBUG);
                            } catch (Exception e) {
                                Output.print("Could not compute decrypted hash: " + e.getMessage(), Status.DEBUG);
                            }
                            conn.send(ResponsePacket.toJson(0));
                        } else {
                            conn.send(ResponsePacket.toJson(1, "Decryption failed"));
                        }
                        break;
                        case "EXECUTE":
                            ExecutePacket executePacket = new ExecutePacket(message);
                            ModalOutput status = executeArgs(breakDownArgs(executePacket.getCommand()));
                            conn.send(ResponsePacket.toJson(status.outputStatusToInt(), status.getOutput()));
                            break;
                        default:
                            Output.print("[CentralMGMTEngine.listen] Unknown packet type received.", Status.BAD);
                            conn.send(ResponsePacket.toJson(1, "Inappropriate Packet."));
                    }
                } catch (Exception e) {
                    Output.print("Invalid JSON: " + e.getMessage());
                    Output.print("[CentralMGMTEngine.listen] JSON parsing failed, raw message: " + message, Status.DEBUG);
                }

                Output.print("[CentralMGMTEngine.listen] Closing WebSocket connection after message handling.", Status.DEBUG);
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
        Output.print("[CentralMGMTEngine.breakDownArgs]: Raw command string: " + s, Status.DEBUG);
        String[] words = s.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("^\\s+|\\s+$", ""); //.replaceAll("[^\\w.:]", "");
            //System.err.println(words[i]);
            Output.print("[CentralMGMTEngine.breakDownArgs]: Parsed word: " + words[i], Status.DEBUG);
        }
        return words;
    }
}