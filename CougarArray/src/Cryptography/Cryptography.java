/*
- privateKey: String
- publicKey: String
- File: File

+setKeys()
+getKeys()
+getFile(String)
*/

package Cryptography;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class Cryptography {
    protected Keys keys;

    //this needs to be consistent when using encryption methods...using AES and RSA combined can cause tomfoolery
    protected static final String transformation = "RSA";

    public Cryptography(String privateKey, String publicKey) {
        this.keys = new Keys(privateKey, publicKey);
    }

    public static Keys generateKeys() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(transformation);
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
}