package CentralManagement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import OutputT.Output;
import OutputT.Status;

//subsystem
//This acts as an event trigger; this parsers then executes the model
public class CentralMGMTEngine {

    private static Map<String, Consumer<String[]>> actions = new HashMap<>();

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
            Output.print("Error: Unknown Command.", Status.BAD);
            Output.print("Available commands: " + actions.keySet());
            return false;
        }
        
        if (parameters.length < 2) {
            Output.print("Error: Command '" + command + "' requires an argument", Status.BAD);
            return false;
        }
        
        try {
            actions.get(command).accept(parameters);
            return true;
        } catch (Exception e) {
            Output.print("Error executing command '" + command + "': " + e.getMessage(), Status.BAD);
            return false;
        }
    }


    // these would be handled in cryptography package (i think)
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
