import CentralManagement.Console;
import Config.config;
import java.io.IOException; 

public class Main {
    public static void main(String[] args) throws IOException {
        config Config = new config();
        
        Console view = new Console();
        view.view();
    }
}