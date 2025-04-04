/*
+ decryptLocally()
+ decryptWithOutsideKey(String)
*/

package com.cougararray.Cryptography;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class Decrypytion {

    private String algorithm; 
    private PrivateKey privateKey;

    public Decrypytion(String algorithm, String privateKey) {
        this.algorithm = algorithm;
        try {
            this.privateKey = Keys.getPrivateKeyFromString(privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public CryptographyResult Decrypt(String Filepath, String output) throws Exception {
        try {
            byte[] fileData = Files.readAllBytes(Paths.get(Filepath + ".enc"));
            byte[] decryptedData = decryptContent(fileData);

            Files.write(Paths.get(output), decryptedData);
            return new CryptographyResult(decryptedData, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CryptographyResult DecryptBytes(byte[] fileData, String output) throws Exception {
        byte[] decryptedData = decryptContent(fileData);
        Files.write(Paths.get(output), decryptedData);
        return new CryptographyResult(decryptedData, true);
    }

    private byte[] decryptContent(byte[] content) throws Exception {
        Cipher cipher = Cipher.getInstance(this.algorithm);
        cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
        return cipher.doFinal(content);
    }
}