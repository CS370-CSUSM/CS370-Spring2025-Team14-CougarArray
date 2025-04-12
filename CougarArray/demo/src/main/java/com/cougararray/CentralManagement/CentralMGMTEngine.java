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
import com.cougararray.TCPWebsocket.Packets.ResponsePacket;
import com.cougararray.TCPWebsocket.WebsocketListener;
import com.cougararray.TCPWebsocket.WebsocketSenderClient;

public class CentralMGMTEngine extends WebsocketListener {

    private static final config Config = new config();
    private final Map<String, CommandHandler> commandMap = new HashMap<>();

    public CentralMGMTEngine() {
        super(Config.getPort());
        this.start(); // Start Websocket

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


    /* @TODO!
     * Make Output look much nicer
     * 
     * ex of each command usage
     * encrypt <fileName> (LOCAL USAGE)
     * decrypt <fileName> (LOCAL USAGE)
     * adduser (address)
     * users
     * send <filePath> <"name"/"address"> <Name / Address of Device> - send private.txt name Lenny
     * ping <address>
     * mykeys
     * help
     * deleteUser <address>
     */
    private void initializeCommandMap() {
        commandMap.put("encrypt", params -> {
            if (params.length > 1) return encryptFile(params[1]);
            return Output.errorPrint("Error: Missing filename for encrypt");
        });

        commandMap.put("decrypt", params -> {
            if (params.length > 1) return decryptFile(params[1]);
            return Output.errorPrint("Error: Missing filename for decrypt");
        });

        commandMap.put("adduser", params -> {
            if (params.length > 1) {
                System.out.println("Paste their public key =>");
                BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
                String publicKey = r.readLine();
                System.out.println("What is the device's name =>");
                String name = r.readLine();
                return addUser(params[1], publicKey, name);
            }
            return Output.errorPrint("Error: Missing address for adduser");
        });

        commandMap.put("users", params -> listUsers());

        // send (file_path) (name/address) (name / address from database)
        commandMap.put("send", params -> {
            if (params.length > 3) {
                Output.print(params[2].toLowerCase().contains("name"));
                if (params[2].toLowerCase().contains("name")) {
                    return sendFile(params[1], new RecordValue(ColumnName.NAME, params[3]));
                } else if (params[2].toLowerCase().contains("address")) {
                    return sendFile(params[1], new RecordValue(ColumnName.IP_ADDRESS, params[3]));
                }
                return Output.errorPrint("Error: Invalid option for send command. Use 'name' or 'address'.");
            }
            return Output.errorPrint("Error: Insufficient parameters for send");
        });

        commandMap.put("ping", params -> {
            if (params.length > 1) {
                WebsocketSenderClient.sendPing(params[1]);
                return true;
            }
            return Output.errorPrint("Error: Missing address for ping");
        });

        commandMap.put("mykeys", params -> {
            Output.print("\n----\nPublic Key: " + Config.getPublicKey() + "\n----\n" +
                       "Private Key (DO NOT SHARE): " + Config.getPrivatekey() + "\n----");
            return true;
        });

        commandMap.put("help", params -> {
            Output.print("Available commands:");
            commandMap.keySet().forEach(cmd -> Output.print("  " + cmd));
            return true;
        });

        commandMap.put("deleteUser", params -> { //deleteUser <address>
            recipientdoa newUser = new recipientdoa(new RecordValue(ColumnName.IP_ADDRESS, params[1]));
            return newUser.deleteuser();
        });
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
}