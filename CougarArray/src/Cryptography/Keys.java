package Cryptography;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Keys {
    private String privateKey;
    private String publicKey;
    private static final String algorithm = "RSA";

    Keys(String privateKey, String publicKey) {
        setPrivate(privateKey);
        setPublic(publicKey);
    }

    //note that the setters are private.
    //The reason being is that private key and public key should never be modified in any other package
    //and if any modification is needed it needs to be done first to the Config file THEN to here
    //This is better practice to ensure certain variables are not modified during runtime
    private void setPrivate(String privateKey) {
        this.privateKey = privateKey;
    }

    private void setPublic(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivate() {
        return this.privateKey;
    }

    public String getPublic() {
        return this.publicKey;
    }


    //This code was made by ChatGPT!
    //The idea is this; when using Java Cipher & Security packages they don't want "publickey" and "privatekey"
    //as String...rather as the class PublicKey & PrivateKey
    //Again this code acts as getters meaning that privatekey & publicKey should NEVER be modified here

    public PublicKey getPublicKeyFromString() throws Exception {
        byte[] byteKey = Base64.getDecoder().decode(this.publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(byteKey);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(keySpec);
    }

    public PrivateKey getPrivateKeyFromString() throws Exception {
        byte[] byteKey = Base64.getDecoder().decode(this.privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(byteKey);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePrivate(keySpec);
    }
}