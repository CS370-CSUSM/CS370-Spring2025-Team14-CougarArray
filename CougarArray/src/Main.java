import CentralManagement.Console;
import Config.config;
import TCPWebsocket.Websocket;
import java.io.IOException; 

public class Main {

    //Config is designed in a way so that it values are NOT modified during runtime
    private static final config Config = new config();

    public static void main(String[] args) throws IOException {
        if (Config.getAsReciever()) {
            Websocket websocket = new Websocket(Config.getPort()); 
            websocket.start(); //begin Websocket process on another thread
        }
        
        Console.view(); //begin View
    }
}