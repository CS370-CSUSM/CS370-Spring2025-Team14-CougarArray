package CentralManagement;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import Config.config;
import Cryptography.CryptographyClient;
import OutputT.Output;
import OutputT.Status;

import TCPWebsocket.WebsocketListener;

//subsystem
//This acts as an event trigger; this parsers then executes the model
public class CentralMGMTEngine extends WebsocketListener {

    private static Map<String, Consumer<String[]>> actions = new HashMap<>();
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
        

        actions.put("encrypt", filepath -> encryptFile(filepath));
        actions.put("decrypt", filepath -> decryptFile(filepath));
        
        //@TODO!
        //Make it so if actAsSender is false then send function cannot be used
        //Also look more into Mapping Runnables so that 2 parameters can be accepted
    }

    public static boolean executeArgs(String[] parameters) {
        if (parameters.length == 0) return Output.errorPrint("Error: No command provided");
        
        String command = parameters[0].toLowerCase(); // case-insensitive commands
        
        if (!actions.containsKey(command)) return Output.errorPrint("Error: Unknown Command. Commands availiable: " + actions.keySet());
        
        if (parameters.length < 2) return Output.errorPrint("Error: Command '" + command + "' requires an argument"); //NOTE; I feel like this would be more appropriate in the action files...?
        
        try {
            actions.get(command).accept(parameters);
            return true;
        } catch (Exception e) {
            return Output.errorPrint("Error executing command '" + command + "': " + e.getMessage());
        }
    }


    //encryptFile
    //make <<use>> of Encryption.java
    //Example Execution
    //  encrypt file.txt
    //  encrypt full/fill/path/to/file.txt
    //  (parameter[1] is file)
    //Expected Outcome:
    //It should generate an encrypted version of the file
    private boolean encryptFile(String[] parameters) {

        return false;
    }

    private boolean decryptFile(String[] parameters) {
        return false;
    }

    private boolean sendFile(String[] parameters) {
        return false;
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
