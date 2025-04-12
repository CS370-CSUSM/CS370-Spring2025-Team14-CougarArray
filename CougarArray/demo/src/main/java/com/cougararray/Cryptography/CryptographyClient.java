package com.cougararray.Cryptography;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

//This is a subsytem that has both Encryption & Decryption in one place
public class CryptographyClient {

    private static Encryption encryption;
    private static Decrypytion decrypytion;
    private static final String algorithm = "RSA";

    //LOCAL ENCRYPTION & DECRYPTION
    private static final SecretKey localAESKey = getAESKeyFromString("f7WR0m1rkaaiD968N9/Bd7M1jC/Y7pZ5F80jszBdPIY=");



    public CryptographyClient(Keys keys) {
        encryption = new Encryption(algorithm, keys.getPublic(), localAESKey);
        decrypytion = new Decrypytion(algorithm, keys.getPrivate(), null);
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
        testEngine.encrypt("test.txt");   
        testEngine.decrypt("test.txt", "testoutput.txt");
    }

    //Public Functions
    public boolean encrypt(String filePath) {
        CryptographyResult output = encryptCallMethod(filePath);
        if (output == null) return false;
        else return output.successful();
    }

    public boolean decrypt(String filePath) {
        CryptographyResult output = decryptCallMethod(filePath, filePath);
        if (output == null) return false;
        else return output.successful();
    }

    public boolean decrypt(String filePath, String fileOutput) {
        CryptographyResult output = decryptCallMethod(filePath, fileOutput);
        if (output == null) return false;
        else return output.successful();
    }

    //Private Functions
    private CryptographyResult encryptCallMethod(String filePath)
    {
        try {
            return encryption.Encrypt(filePath);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    private CryptographyResult decryptCallMethod(String filePath, String fileOutput)
    {
        try {
            return decrypytion.Decrypt(filePath, fileOutput, localAESKey);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    //Static usage; we are not using variables created inside here
    public static CryptographyResult encrypt(String filepath, String publicKey) {
        CryptographyResult output = new CryptographyResult(null, false);
        Encryption encryptionLocal = new Encryption(algorithm, publicKey, generateTransactionkey()); //temp variable
        try {
            output = encryptionLocal.Encrypt(filepath, publicKey);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return output;
    }

    //This is mostly going to be used externally
    public static boolean decryptBytes(byte[] content, String output, String privateKey, byte[] aesKey)
    {
        Decrypytion decryptionLocal = new Decrypytion(algorithm, privateKey, aesKey);
        try {
            return decryptionLocal.DecryptBytes(content, output).successful;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    private static SecretKey generateTransactionkey() {
        KeyGenerator aesKeyGen;
        try {
            aesKeyGen = KeyGenerator.getInstance("AES");
            aesKeyGen.init(256); // AES-256
            return aesKeyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static SecretKey getAESKeyFromString(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        // "AES" tells Java this is an AES key
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
    
}

//TODO
//Make it so where it asks to input "public key or private key", just replace it with Key class
//I been having headache because putting in the wrong key really does screw everything up
//i hate this.

//TODO
//make a unit test