package CentralManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

//subsystem
//This does NOT focus on execution; only on acting as a View
public class Console extends CentralMGMTEngine {

    public static void view() throws IOException {

        while(true)
        {
            CentralMGMTEngine engine = new CentralMGMTEngine();
            //System.out.print("> "); @TODO! Make it so when there is console output, it doesn't break this

            // Enter data using BufferReader
            BufferedReader r = new BufferedReader(
                new InputStreamReader(System.in));

            // Reading data using readLine
            String s = r.readLine();
            String[] args = breakDownArgs(s);
            engine.executeArgs(args);
        }
    }

    private static String[] breakDownArgs(String s){
        String[] words = s.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            words[i] = words[i].replaceAll("[^\\w]", "");
        }
        return words;
    }
}
