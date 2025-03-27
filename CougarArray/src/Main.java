import CentralManagement.Console;
import Config.config;
import TCPWebsocket.Websocket;

import java.io.IOException; 

public class Main {
    public static void main(String[] args) throws IOException {
        config Config = new config();
        startup(Config);
        
        Console view = new Console();
        view.view();
    }

    private static boolean startup(config Config) {
        Websocket websocket = new Websocket(Config.getPort());
        websocket.start();
        return false;
    }
}