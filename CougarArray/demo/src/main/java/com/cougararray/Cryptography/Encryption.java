/*
+ encryptLocally()
+ encryptWithOutsideKey(String)
*/

package com.cougararray.Cryptography;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class Encryption {

    private String algorithm; 
    private PublicKey publicKey;
    private SecretKey aesKey;
    
    public Encryption(String algorithm, String publicKey, SecretKey aesKey) {
        this.aesKey = aesKey;
        this.algorithm = algorithm;
        try {
            this.publicKey = Keys.getPublicKeyFromString(publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Local Use
    public CryptographyResult Encrypt(String Filepath) throws Exception {

        byte[] fileData = Files.readAllBytes(Paths.get(Filepath));
        byte[] encryptedData = encryptContent(fileData);
        
        //not needed
        //byte[] encryptedKey = encryptAESKey(aesKey, externalPublicKey);

        Files.write(Paths.get(Filepath + ".enc"), encryptedData);
        return new CryptographyResult(encryptedData, true, null);

    }

    //External Use
    public CryptographyResult Encrypt(String Filepath, String publicKey) throws Exception {

        PublicKey externalPublicKey = Keys.getPublicKeyFromString(publicKey);

        byte[] fileData = Files.readAllBytes(Paths.get(Filepath));
        byte[] encryptedData = encryptContent(fileData);
        byte[] encryptedKey = encryptAESKey(externalPublicKey);

        //Files.write(Paths.get(Filepath + ".enc"), encryptedData);
        return new CryptographyResult(encryptedData, true, encryptedKey);

    }

    //cipher documentation i was reading

    private byte[] encryptContent(byte[] content) throws Exception  {
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, this.aesKey);
        return aesCipher.doFinal(content);
    }

    private byte[] encryptAESKey(PublicKey publicKey) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return rsaCipher.doFinal(this.aesKey.getEncoded());
    }

}