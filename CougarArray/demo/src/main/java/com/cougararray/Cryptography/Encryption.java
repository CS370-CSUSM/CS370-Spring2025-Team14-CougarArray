/*
+ encryptLocally()
+ encryptWithOutsideKey(String)
*/

package com.cougararray.Cryptography;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.crypto.Cipher;

public class Encryption extends Cryptography {
    
    public Encryption(String privateKey, String publicKey) {
        super(privateKey, publicKey);
    }

    public boolean Encrypt(String Filepath) throws Exception {
        try {
            byte[] fileData = Files.readAllBytes(Paths.get(Filepath));
            byte[] encryptedData = encryptContent(fileData);

            Files.write(Paths.get(Filepath + ".enc"), encryptedData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    //cipher documentation i was reading

    private byte[] encryptContent(byte[] content) throws Exception  {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, this.keys.getPublicKeyFromString());
        return cipher.doFinal(content);

    }
}