/*
+ decryptLocally()
+ decryptWithOutsideKey(String)
*/

package com.cougararray.Cryptography;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.cougararray.Config.config;;
public class Decrypytion {

    private static final config Config = new config();
    private PrivateKey privateKey;
    private byte[] encryptedAESKey;

    public Decrypytion(String algorithm, String privateKey, byte[] encryptedAESKey) {
        this.encryptedAESKey = encryptedAESKey;
        try {
            this.privateKey = Keys.getPrivateKeyFromString(privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CryptographyResult Decrypt(String Filepath, String output, SecretKey localAESKey) throws Exception
    {
        byte[] fileData = Files.readAllBytes(Paths.get(Filepath));
        byte[] decryptedData = decryptContent(fileData, localAESKey);

        Files.write(Paths.get(Config.getDecryptedPrefix()+output), decryptedData);
        return new CryptographyResult(decryptedData, true);
    }
    
    public CryptographyResult Decrypt(String Filepath, String output) throws Exception {
        byte[] fileData = Files.readAllBytes(Paths.get(Filepath));
        byte[] decryptedData = decryptContent(fileData, decryptAESKey(this.privateKey));

        Files.write(Paths.get(Config.getDecryptedPrefix()+output), decryptedData);
        return new CryptographyResult(decryptedData, true);
    }

    public CryptographyResult DecryptBytes(byte[] fileData, String output) throws Exception {
        byte[] decryptedData = decryptContent(fileData, decryptAESKey(this.privateKey));
        Files.write(Paths.get(output), decryptedData);
        return new CryptographyResult(decryptedData, true);
    }

    private byte[] decryptContent(byte[] encryptedContent, SecretKey aesKey) throws Exception {
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        return aesCipher.doFinal(encryptedContent);
    }    

    private SecretKey decryptAESKey(PrivateKey privateKey) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedKey = rsaCipher.doFinal(this.encryptedAESKey);
        return new SecretKeySpec(decryptedKey, "AES");
    }
}