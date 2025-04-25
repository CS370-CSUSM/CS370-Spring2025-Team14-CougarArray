package com.cougararray.CentralManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;

public class Console extends CentralMGMTEngine {

    public Console() {
        super();
    }

    /**
     * Accepting input commands from user
     */
    public void view() {
        // single shared reader on stdin
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // 500ms delay for initialization message
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("-".repeat(79)); // 79 dashes matches other output lengths
        Output.print("CougarArray initialized!", Status.GOOD);
        Output.print("CougarArray allows you to encrypt, decrypt, send, and receive files over your local network!", Status.DASH);
        Output.print("To get started, type 'help' to see available commands", Status.DASH);

        while (true) {
            System.out.print(">>> ");

            String line;
            try {
                line = reader.readLine();
            } catch (IOException ioe) {
                // I/O error reading the command
                Output.errorPrint("I/O error: " + ioe.getMessage());
                continue;
            }

            if (line == null) {
                // EOF (e.g. Ctrl-D), exit cleanly
                Output.print("Input stream closed, shutting down.", Status.GOOD);
                break;
            }

            String[] args = breakDownArgs(line);

            try {
                // executeArgs can itself throw IOException if any handler leaks it
                System.out.println(executeArgs(args).getOutput());
            } catch (IOException ioe) {
                Output.errorPrint("Error running command: " + ioe.getMessage());
            }
        }
    }
}
