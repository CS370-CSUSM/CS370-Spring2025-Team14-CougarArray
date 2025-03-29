package Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import OutputT.Output;
import OutputT.Status;

public class config {

    private static final String FILE_PATH = "config.properties";

    //DEFAULT VALUES
    private int port = 5666; //by default, operate as a reciever on port 5666
    private boolean actAsSender = true; //by default, you can send files
    private boolean actAsReciever = true; //by default, you can recieve files

    private String privateKey = null;
    private String publicKey = null;

    public int getPort() {
        return port;
    }
    
    public boolean getAsSender() {
        return actAsSender;
    }

    public boolean getAsReciever() {
        return actAsReciever;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivatekey() {
        return privateKey;
    }

    private boolean updateProperty(String key, String value) {

        Output.print("Updating properties key " + key + " to value: " + value);

        try (FileInputStream in = new FileInputStream(FILE_PATH)) {
            Properties props = new Properties();
            props.load(in);
            props.setProperty(key, value);
            try (FileOutputStream out = new FileOutputStream(FILE_PATH)) {
                props.store(out, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Output.print("Successfully updated config.properties." , Status.GOOD);
        return true;
    }

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
                this.port = Integer.parseInt(properties.getProperty("Port", Integer.toString(this.port)));
                this.actAsSender = Boolean.parseBoolean(properties.getProperty("actAsSender", Boolean.toString(this.actAsSender)));
                this.actAsReciever = Boolean.parseBoolean(properties.getProperty("actAsReceiver", Boolean.toString(this.actAsReciever)));
                this.publicKey = properties.getProperty("publicKey", null);
                this.privateKey = properties.getProperty("privateKey", null);
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error reading properties file: " + e.getMessage());
            }

            return;
        }

        generateConfig(properties, file);
    }

    private void generateConfig(Properties properties, File file) {

        // Case #2: The Properties file is not made; Generate it
        properties.setProperty("Port", Integer.toString(this.port));
        properties.setProperty("actAsSender", Boolean.toString(actAsSender));
        properties.setProperty("actAsReciever", Boolean.toString(actAsReciever));
        properties.setProperty("privateKey", null);
        properties.setProperty("publicKey", null);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "Configuration Settings");
            System.out.println("Config file created with default values.");
        } catch (IOException e) {
            System.err.println("Error creating properties file: " + e.getMessage());
        }

    }
}
