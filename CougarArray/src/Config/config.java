package Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class config {

    private static final String FILE_PATH = "config.properties";

    //DEFAULT VALUES
    private int port = 5666; //by default, operate as a reciever on port 5666
    private boolean actAsSender = true; //by default, you can send files
    private boolean actAsReciever = true; //by default, you can recieve files

    public config() {
        loadConfig();
    }

    //This should only be executed once at runtime and never again by client
    private void loadConfig() {
        File file = new File(FILE_PATH);
        Properties properties = new Properties();

        // Case #1: If file exists, load it
        if (file.exists()) {
            System.out.println("Loading Config File...");
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
                this.port = Integer.parseInt(properties.getProperty("Port", "2020"));
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error reading properties file: " + e.getMessage());
            }

            return;
        }

        // Case #2: The Properties file is not made; Generate it
        properties.setProperty("Port", "5666");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "Configuration Settings");
            System.out.println("Config file created with default values.");
        } catch (IOException e) {
            System.err.println("Error creating properties file: " + e.getMessage());
        }

    }
}
