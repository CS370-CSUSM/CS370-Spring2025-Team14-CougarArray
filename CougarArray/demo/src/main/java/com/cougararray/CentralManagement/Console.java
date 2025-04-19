package com.cougararray.CentralManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;

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

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("--------------------------------------");
        Output.print("CougarArray initialized!", Status.GOOD);
        Output.print("To see available commands, type help", Status.GOOD);

        while(true)
        {
            // >>> indicating it is the user's "turn" (is one '>' preferred?)
            System.out.print(">>> ");

            // Enter data using BufferReader
            BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

            // Reading data using readLine
            String s = r.readLine();
            String[] args = breakDownArgs(s);
            System.out.println(executeArgs(args).getOutput());
        }
    }
}
