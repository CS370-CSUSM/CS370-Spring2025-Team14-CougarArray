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

    private String privateKey = null;
    private String publicKey = null;
    private String port = null;
    private String debugMode = null;
    private String actAsSender = null;
    private String actAsReceiver = null;


    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivatekey() {
        return privateKey;
    }

    public String getPort() {
        return port;
    }

    public String getDebug() {
        return debugMode;
    }

    public String getActAsSender() {
        return actAsSender;
    }

    public String getActAsReceiver() {
        return actAsReceiver;
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
        Output.print("Error setting keys", Status.BAD);
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
            Output.print("Error updating properties key: " + e.getMessage(), Status.BAD);
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
            Output.print("Loading Config File...");
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
                this.publicKey = properties.getProperty("publicKey", null);
                this.privateKey = properties.getProperty("privateKey", null);
                this.actAsReceiver = properties.getProperty("actAsReceiver", null);
                this.actAsSender = properties.getProperty("actAsSender",  null);
                this.port = properties.getProperty("port", null);
                this.debugMode = properties.getProperty("debugMode", null);
            } catch (IOException | NumberFormatException e) {
                Output.print("Error reading properties file: " + e.getMessage(), Status.BAD);
            }

            return;
        }

        generateConfig(properties, file);
    }

    private void generateConfig(Properties properties, File file) {

        // Case #2: The Properties file is not made; Generate it
        properties.setProperty("privateKey", "");
        properties.setProperty("publicKey", "");
        properties.setProperty("port", "5666");
        properties.setProperty("actAsSender", "true");
        properties.setProperty("actAsReceiver", "true");
        properties.setProperty("debugMode", "true");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "Configuration Settings");
            Output.print("Config file created with default values.");
        } catch (IOException e) {
            Output.print("Error creating properties file: " + e.getMessage(), Status.BAD);
        }

    }
}
