package CentralManagement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import Config.config;
import Cryptography.Cryptography;
import Cryptography.CryptographyClient;
import OutputT.Output;
import OutputT.Status;

//subsystem
//This acts as an event trigger; this parsers then executes the model
public class CentralMGMTEngine {

    private static Map<String, Consumer<String[]>> actions = new HashMap<>();
    private config Config = new config();

    public CentralMGMTEngine() {

        Output.print("test");

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
}
