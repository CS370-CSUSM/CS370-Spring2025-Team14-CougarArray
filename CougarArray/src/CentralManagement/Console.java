package CentralManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Console {
    public static void console(String[] args) throws IOException {

        while(true)
        {
            // Enter data using BufferReader
            BufferedReader r = new BufferedReader(
                new InputStreamReader(System.in));

            // Reading data using readLine
            String s = r.readLine();
        }
    }
}
