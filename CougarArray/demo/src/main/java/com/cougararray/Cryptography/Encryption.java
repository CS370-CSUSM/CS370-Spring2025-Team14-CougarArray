/*
+ encryptLocally()
+ encryptWithOutsideKey(String)
*/

package com.cougararray.Cryptography;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class Encryption {

    private String algorithm; 
    private PublicKey publicKey;
    
    public Encryption(String algorithm, String publicKey) {
        this.algorithm = algorithm;
        try {
            this.publicKey = Keys.getPublicKeyFromString(publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CryptographyResult Encrypt(String Filepath) throws Exception {

        byte[] fileData = Files.readAllBytes(Paths.get(Filepath));
        byte[] encryptedData = encryptContent(fileData);

        Files.write(Paths.get(Filepath + ".enc"), encryptedData);
        return new CryptographyResult(encryptedData, true);

    }

    //cipher documentation i was reading

    private byte[] encryptContent(byte[] content) throws Exception  {
        Cipher cipher = Cipher.getInstance(this.algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
        return cipher.doFinal(content);
    }
}