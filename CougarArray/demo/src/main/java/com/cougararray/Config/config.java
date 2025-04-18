package com.cougararray.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.cougararray.Cryptography.Keys;
import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;

public class config {

    private static final String FILE_PATH = "config.properties";

    //DEFAULT VALUES
    private int port = 5666; //by default, operate as a receiver on port 5666
    private boolean actAsSender = true; //by default, you can send files
    private boolean actAsReceiver = true; //by default, you can receive files

    private String privateKey = null;
    private String publicKey = null;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (updateProperty("port", String.valueOf(port))) {
            this.port = port;
        }
    }
    
    public boolean getAsSender() {
        return actAsSender;
    }

    public boolean getAsReceiver() {
        return actAsReceiver;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivatekey() {
        return privateKey;
    }

    public boolean emptyOrInvalidKeys() {
        return (this.publicKey == null || this.publicKey.isEmpty()) || (this.privateKey == null || this.privateKey.isEmpty());
    }

    public boolean setKeys(Keys keys) {
        if (updateProperty("privateKey", keys.getPrivate()) & updateProperty("publicKey", keys.getPublic())) {
            this.publicKey = keys.getPublic();
            this.privateKey = keys.getPrivate();
            return true;
        }
        return false;
    }

    public Keys getKeys(){
        return new Keys(getPrivatekey(), getPublicKey());
    }

    //This should be kept private due to the severity of this
    //we making illegal code with this one
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
            return false;
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
                this.actAsReceiver = Boolean.parseBoolean(properties.getProperty("actAsReceiver", Boolean.toString(this.actAsReceiver)));
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
        properties.setProperty("actAsReceiver", Boolean.toString(actAsReceiver));
        properties.setProperty("privateKey", "");
        properties.setProperty("publicKey", "");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "Configuration Settings");
            System.out.println("Config file created with default values.");
        } catch (IOException e) {
            System.err.println("Error creating properties file: " + e.getMessage());
        }

    }
}
