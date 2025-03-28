package CentralManagement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

//subsystem
//This acts as an event trigger; this parsers then executes the model
public class CentralMGMTEngine {

    private static Map<String, Consumer<String>> actions = new HashMap<>();

    public CentralMGMTEngine() {
        actions.put("encrypt", filepath -> encryptFile(filepath));
        actions.put("decrypt", filepath -> decryptFile(filepath));
        
        //@TODO!
        //Make it so if actAsSender is false then send function cannot be used
        //Also look more into Mapping Runnables so that 2 parameters can be accepted
    }

    public static boolean executeArgs(String[] parameters) {
        if (parameters.length == 0) {
            System.err.println("Error: No command provided");
            return false;
        }
        
        String command = parameters[0].toLowerCase(); // case-insensitive commands
        
        if (!actions.containsKey(command)) {
            System.err.println("Error: Unknown command '" + command + "'");
            System.err.println("Available commands: " + actions.keySet());
            return false;
        }
        
        if (parameters.length < 2) {
            System.err.println("Error: Command '" + command + "' requires an argument");
            return false;
        }
        
        try {
            actions.get(command).accept(parameters[1]);
            return true;
        } catch (Exception e) {
            System.err.println("Error executing command '" + command + "': " + e.getMessage());
            return false;
        }
    }

    private boolean encryptFile(String filepath) {
        return false;
    }

    private boolean decryptFile(String filepath) {
        return false;
    }

    private boolean sendFile(String filepath) {
        return false;
    }
}
