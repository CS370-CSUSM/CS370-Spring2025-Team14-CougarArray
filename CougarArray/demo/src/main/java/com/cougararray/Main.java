package com.cougararray;

//import CentralManagement.Console;
//import Config.config;
//import TCPWebsocket.WebsocketListener;
import java.io.IOException;

import com.cougararray.CentralManagement.Console;
/**
 * Main entry point for CougarArray
 * Initializes and displays console
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Console view = new Console();
        view.view();
    }
}