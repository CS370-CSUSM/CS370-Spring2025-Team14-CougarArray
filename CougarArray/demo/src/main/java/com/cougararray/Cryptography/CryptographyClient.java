package com.cougararray.Cryptography;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

//This is a subsytem that has both Encryption & Decryption in one place
public class CryptographyClient {

    public Encryption encryption;
    public Decrypytion decrypytion;
    private static final String algorithm = "RSA";

    public CryptographyClient(Keys keys) {
        encryption = new Encryption(keys.getPrivate(), keys.getPublic());
        decrypytion = new Decrypytion(keys.getPrivate(), keys.getPublic());
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
    
}

//TODO
//Make it so where it asks to input "public key or private key", just replace it with Key class
//I been having headache because putting in the wrong key really does screw everything up
//i hate this.
