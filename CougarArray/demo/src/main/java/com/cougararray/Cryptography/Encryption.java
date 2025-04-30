/*
+ encryptLocally()
+ encryptWithOutsideKey(String)
*/

package com.cougararray.Cryptography;


import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;
import com.cougararray.Config.config;

public class Encryption {

    private static final config Config = new config();
    private SecretKey aesKey;
    
    public Encryption(String algorithm, String publicKey, SecretKey aesKey) {
        this.aesKey = aesKey;
        try {
            Keys.getPublicKeyFromString(publicKey);
        } catch (Exception e) {
            Output.print("Error reading publicKey in Encryption: " + e.getMessage(), Status.BAD);
        }
    }

    //Local Use 
    public CryptographyResult Encrypt(String Filepath) throws Exception {
        byte[] fileData = Files.readAllBytes(Paths.get(Filepath));
        String originalHash = FileHasher.hashBytes(fileData);
        Output.print("[Encryption.Encrypt] Original file hash: " + originalHash, Status.DEBUG);
        
        byte[] encryptedData = encryptContent(fileData);
        String encryptedHash = FileHasher.hashBytes(encryptedData);
        Output.print("[Encryption.Encrypt] Encrypted data hash: " + encryptedHash, Status.DEBUG);
    
        Files.write(Paths.get(Filepath + Config.getEncryptedSuffix()), encryptedData);
        return new CryptographyResult(encryptedData, true, null);
    }
    

    //External Use
    public CryptographyResult Encrypt(String Filepath, String publicKey) throws Exception {
        PublicKey externalPublicKey = Keys.getPublicKeyFromString(publicKey);
        byte[] fileData = Files.readAllBytes(Paths.get(Filepath));
        String originalHash = FileHasher.hashBytes(fileData);
        Output.print("[Encryption.Encrypt] Original file hash: " + originalHash, Status.DEBUG);
    
        byte[] encryptedData = encryptContent(fileData);
        String encryptedHash = FileHasher.hashBytes(encryptedData);
        Output.print("[Encryption.Encrypt] Encrypted data hash: " + encryptedHash, Status.DEBUG);
    
        byte[] encryptedKey = encryptAESKey(externalPublicKey);
        return new CryptographyResult(encryptedData, true, encryptedKey);
    }

    //cipher documentation i was reading

    private byte[] encryptContent(byte[] content) throws Exception  {
    Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
    aesCipher.init(Cipher.ENCRYPT_MODE, this.aesKey);
    byte[] iv = aesCipher.getIV();
    byte[] encryptedBytes = aesCipher.doFinal(content);
    // Prepend IV to encrypted data
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(iv);
    outputStream.write(encryptedBytes);
    return outputStream.toByteArray();
}

    private byte[] encryptAESKey(PublicKey publicKey) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return rsaCipher.doFinal(this.aesKey.getEncoded());
    }

}