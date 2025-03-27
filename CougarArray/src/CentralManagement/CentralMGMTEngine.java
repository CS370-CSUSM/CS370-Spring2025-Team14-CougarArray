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
        String command = parameters[0]; //send, encrypt, decrypt

        actions.get(command).accept(parameters[1]);

        return false;
    }

    private boolean encryptFile(String filepath){
        return false;
    }

    private boolean decryptFile(String filepath) {
        return false;
    }

    private boolean sendFile(String filepath) {
        return false;
    }
}
