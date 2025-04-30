/*
+ decryptLocally()
+ decryptWithOutsideKey(String)
*/

package com.cougararray.Cryptography;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.cougararray.Config.config;
import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;;
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

    public CryptographyResult Decrypt(String Filepath, String output, SecretKey localAESKey) throws Exception {
        byte[] fileData = Files.readAllBytes(Paths.get(Filepath));
        byte[] decryptedData = decryptContent(fileData, localAESKey);

        // Get original file's directory
        Path encryptedPath = Paths.get(Filepath);
        Path parentDir = encryptedPath.getParent();
        // Add null check
        if (parentDir == null) {
            parentDir = Paths.get(""); // Fallback to current directory
        }
                
        // Build output filename
        String originalFilename = encryptedPath.getFileName().toString();
        if (originalFilename.endsWith(Config.getEncryptedSuffix())) {
            originalFilename = originalFilename.substring(
                0, 
                originalFilename.length() - Config.getEncryptedSuffix().length()
            );
        }
        String decryptedFilename = Config.getDecryptedPrefix() + originalFilename;
        
        // Create full output path
        Path outputPath = parentDir.resolve(sanitizeFilename(decryptedFilename));
        
        Files.write(outputPath, decryptedData);
        return new CryptographyResult(decryptedData, true);
    }

        public CryptographyResult Decrypt(String Filepath, String output) throws Exception {
        byte[] fileData = Files.readAllBytes(Paths.get(Filepath));
        String encryptedHash = FileHasher.hashBytes(fileData);
        Output.print("[Decryption.Decrypt] Encrypted file hash: " + encryptedHash, Status.DEBUG);

        byte[] decryptedData = decryptContent(fileData, decryptAESKey(this.privateKey));
        String decryptedHash = FileHasher.hashBytes(decryptedData);
        Output.print("[Decryption.Decrypt] Decrypted data hash: " + decryptedHash, Status.DEBUG);


        Path encryptedPath = Paths.get(Filepath);
        Path parentDir = encryptedPath.getParent();
        // Add null check
        if (parentDir == null) {
            parentDir = Paths.get(""); // Fallback to current directory
        }
        
        String originalFilename = encryptedPath.getFileName().toString();
        if (originalFilename.endsWith(Config.getEncryptedSuffix())) {
            originalFilename = originalFilename.substring(
                0, 
                originalFilename.length() - Config.getEncryptedSuffix().length()
            );
}
        String decryptedFilename = Config.getDecryptedPrefix() + originalFilename;
        
        Path outputPath = parentDir.resolve(sanitizeFilename(decryptedFilename));
        
        Files.write(outputPath, decryptedData);
        return new CryptographyResult(decryptedData, true);
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public CryptographyResult DecryptBytes(byte[] fileData, String output) throws Exception {
        byte[] decryptedData = decryptContent(fileData, decryptAESKey(this.privateKey));
        Files.write(Paths.get(output), decryptedData);
        return new CryptographyResult(decryptedData, true);
    }

    private byte[] decryptContent(byte[] encryptedContent, SecretKey aesKey) throws Exception {
    // Extract IV (first 12 bytes) and ciphertext with tag
    if (encryptedContent.length < 12) {
        throw new IllegalArgumentException("Invalid encrypted content");
    }
    byte[] iv = Arrays.copyOfRange(encryptedContent, 0, 12);
    byte[] ciphertext = Arrays.copyOfRange(encryptedContent, 12, encryptedContent.length);

    Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
    GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 128-bit tag length
    aesCipher.init(Cipher.DECRYPT_MODE, aesKey, spec);
    return aesCipher.doFinal(ciphertext);
}

    private SecretKey decryptAESKey(PrivateKey privateKey) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedKey = rsaCipher.doFinal(this.encryptedAESKey);
        return new SecretKeySpec(decryptedKey, "AES");
    }
}