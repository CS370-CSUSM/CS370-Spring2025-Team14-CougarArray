package CentralManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

//subsystem
public class Console {

    private static CentralMGMTEngine Execution = new CentralMGMTEngine();

    public void view() throws IOException {

        while(true)
        {
            //System.out.print("> "); @TODO! Make it so when there is console output, it doesn't break this

            // Enter data using BufferReader
            BufferedReader r = new BufferedReader(
                new InputStreamReader(System.in));

            // Reading data using readLine
            String s = r.readLine();
            String[] args = breakDownArgs(s);
            executeArgs(args);
        }
    }

    private static String[] breakDownArgs(String s){
        String[] words = s.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("[^\\w]", "");
        }
        return words;
    }

    private static boolean executeArgs(String[] parameters) {
        String command = parameters[0]; //send, encrypt, decrypt

        Map<String, Runnable> functionMap = new HashMap<>();

        return false;
    }
}
