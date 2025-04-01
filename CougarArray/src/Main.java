import CentralManagement.Console;
import Config.config;
import TCPWebsocket.WebsocketListener;
import java.io.IOException; 

public class Main {

    public static void main(String[] args) throws IOException {
        Console view = new Console();
        view.view();
    }
}