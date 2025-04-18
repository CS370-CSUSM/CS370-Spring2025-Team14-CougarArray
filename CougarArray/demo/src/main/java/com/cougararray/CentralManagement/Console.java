package com.cougararray.CentralManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Console provides a CLI for user interaction
 *
 * Primarily acts as the "View"
 * Backend logic will be found in CentralMGMTEngine
 */
public class Console extends CentralMGMTEngine {

    public Console() {
        super();
    }

    /**
     * Accepting input commands from user
     * @throws IOException if read input is invalid
     */
    public void view() throws IOException {

        while(true)
        {
            //System.out.print("> "); @TODO! Make it so when there is console output, it doesn't break this

            // Enter data using BufferReader
            BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

            // Reading data using readLine
            String s = r.readLine();
            String[] args = breakDownArgs(s);
            executeArgs(args);
        }
    }
}
