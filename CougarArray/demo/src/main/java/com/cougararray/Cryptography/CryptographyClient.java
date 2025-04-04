package com.cougararray.Cryptography;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

//This is a subsytem that has both Encryption & Decryption in one place
public class CryptographyClient {

    private Encryption encryption;
    private Decrypytion decrypytion;
    private static final String algorithm = "RSA";

    public CryptographyClient(Keys keys) {
        encryption = new Encryption(algorithm, keys.getPublic());
        decrypytion = new Decrypytion(algorithm, keys.getPrivate());
    }

    public static Keys generateKeys() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algorithm);
            keyPairGen.initialize(2048); // Use 2048-bit RSA key
            KeyPair keyPair = keyPairGen.generateKeyPair();

            PublicKey pubKey = keyPair.getPublic();
            PrivateKey privKey = keyPair.getPrivate();

            String publicKey = Base64.getEncoder().encodeToString(pubKey.getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(privKey.getEncoded());
            return new Keys(privateKey, publicKey);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws Exception {

        Keys keys = generateKeys();

        CryptographyClient testEngine = new CryptographyClient(keys);
        testEngine.encryption.Encrypt("test.txt");   
        testEngine.decrypytion.Decrypt("test.txt", "testoutput.txt");
    }

    public boolean encrypt(String filePath) {
        try {
            return encryption.Encrypt(filePath).successful;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public boolean decrypt(String filePath) {
        try {
            return decrypytion.Decrypt(filePath, filePath);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    //Static usage; we are not using variables created inside here
    public static CryptographyResult encryptWithOutsideKey(String filepath, String publicKey) {
        Encryption encryption = new Encryption(null, publicKey); //make a new encryption variable but only use public key as we are only performing encryption
        CryptographyResult output = new CryptographyResult(null, false);
        try {
            output = encryption.Encrypt(filepath);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return output;
    }
    
}

//TODO
//Make it so where it asks to input "public key or private key", just replace it with Key class
//I been having headache because putting in the wrong key really does screw everything up
//i hate this.

//TODO
//make a unit test