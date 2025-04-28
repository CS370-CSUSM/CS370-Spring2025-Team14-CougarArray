package com.cougararray.Cryptography;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.cougararray.OutputT.Output;
import com.cougararray.OutputT.Status;
import com.cougararray.Config.config;

public class CryptographyClient {

    private static Encryption encryption;
    private static Decrypytion decrypytion;
    private static final String algorithm = "RSA";
    private static final config Config = new config();

    // LOCAL ENCRYPTION & DECRYPTION
    private static final SecretKey localAESKey;

    static {
        SecretKey key = null;
        try {
            key = getAESKeyFromString(Config.getAESKey());
        } catch (IllegalArgumentException e) {
            Output.print("Failed to initialize AES key: " + e.getMessage(), Status.BAD);
            throw new ExceptionInInitializerError("Invalid AES key initialization");
        }
        localAESKey = key;
    }

    public CryptographyClient(Keys keys) {
        if (keys == null) {
            throw new IllegalArgumentException("Keys cannot be null");
        }
        if (keys.getPublic() == null || keys.getPrivate() == null) {
            throw new IllegalArgumentException("Invalid key pair provided");
        }

        try {
            encryption = new Encryption(algorithm, keys.getPublic(), localAESKey);
            decrypytion = new Decrypytion(algorithm, keys.getPrivate(), null);
        } catch (Exception e) {
            throw new CryptographicInitializationException("Failed to initialize cryptographic components", e);
        }
    }

    public static Keys generateKeys() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algorithm);
            keyPairGen.initialize(2048);
            KeyPair keyPair = keyPairGen.generateKeyPair();

            PublicKey pubKey = keyPair.getPublic();
            PrivateKey privKey = keyPair.getPrivate();

            String publicKey = Base64.getEncoder().encodeToString(pubKey.getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(privKey.getEncoded());
            return new Keys(privateKey, publicKey);

        } catch (NoSuchAlgorithmException e) {
            Output.print("Failed to generate keys: " + e.getMessage(), Status.BAD);
            throw new CryptographicOperationException("Key generation failed", e);
        }
    }

    // Main method remains unchanged as per user request
    public static void main(String[] args) throws Exception {
        Keys keys = generateKeys();
        CryptographyClient testEngine = new CryptographyClient(keys);
        testEngine.encrypt("test.txt");
        testEngine.decrypt("test.txt", "testoutput.txt");
    }

    // Public Functions with improved error handling
    public boolean encrypt(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            Output.print("Invalid file path provided for encryption", Status.BAD);
            return false;
        }

        try {
            validateFileExists(filePath);
            CryptographyResult output = encryptCallMethod(filePath);
            
            if (output == null || !output.successful()) {
                Output.print("Encryption of file " + filePath + " failed", Status.BAD);
                return false;
            }
            
            Output.print("Encryption of file " + filePath + " successful", Status.GOOD);
            Output.print("Created file " + filePath, Status.GOOD);
            return true;

        } catch (Exception e) {
            Output.print("Encryption failed: " + e.getMessage(), Status.BAD);
            return false;
        }
    }

    public boolean decrypt(String filePath) {
        String outputPath = filePath.replaceAll("\\" + Config.getEncryptedSuffix() + "$", "");
        return decrypt(filePath, outputPath);
    }

    public boolean decrypt(String filePath, String fileOutput) {
        if (filePath == null || filePath.trim().isEmpty()) {
            Output.print("Invalid input file path provided for decryption", Status.BAD);
            return false;
        }
        if (fileOutput == null || fileOutput.trim().isEmpty()) {
            Output.print("Invalid output file path provided for decryption", Status.BAD);
            return false;
        }

        try {
            validateFileExists(filePath);
            CryptographyResult output = decryptCallMethod(filePath, fileOutput);
            
            if (output == null || !output.successful()) {
                Output.print("Decryption of file " + filePath + "failed", Status.BAD);
                return false;
            }
            
            Output.print("Decryption of file " + filePath + " successful", Status.GOOD);
            String cleanedFilePath = filePath.replaceAll("\\" + Config.getEncryptedSuffix() + "$", "");
            Output.print("Created file decrypted_" + cleanedFilePath , Status.GOOD);
            return true;

        } catch (Exception e) {
            Output.print("Decryption failed: " + e.getMessage(), Status.BAD);
            return false;
        }
    }

    // Private Functions with improved error handling
    private CryptographyResult encryptCallMethod(String filePath) {
        try {
            return encryption.Encrypt(filePath);
        } catch (Exception e) {
            throw new CryptographicOperationException("Encryption operation failed", e);
        }
    }

    private CryptographyResult decryptCallMethod(String filePath, String fileOutput) {
        try {
            return decrypytion.Decrypt(filePath, fileOutput, localAESKey);
        } catch (Exception e) {
            throw new CryptographicOperationException("Decryption operation failed", e);
        }
    }

    // Static methods with improved error handling
    public static CryptographyResult encrypt(String filepath, String publicKey) {
        if (filepath == null || publicKey == null) {
            Output.print("Invalid parameters for encryption", Status.BAD);
            return new CryptographyResult(null, false);
        }

        try {
            validateFileExists(filepath);
            Encryption encryptionLocal = new Encryption(algorithm, publicKey, generateTransactionkey());
            return encryptionLocal.Encrypt(filepath, publicKey);
        } catch (Exception e) {
            Output.print("Static encryption failed: " + e.getMessage(), Status.BAD);
            return new CryptographyResult(null, false);
        }
    }

    public static boolean decryptBytes(byte[] content, String output, String privateKey, byte[] aesKey) {
        if (content == null || privateKey == null || aesKey == null) {
            Output.print("Invalid parameters for byte decryption", Status.BAD);
            return false;
        }

        try {
            Decrypytion decryptionLocal = new Decrypytion(algorithm, privateKey, aesKey);
            return decryptionLocal.DecryptBytes(content, output).successful();
        } catch (Exception e) {
            Output.print("Byte decryption failed: " + e.getMessage(), Status.BAD);
            return false;
        }
    }

    // Utility methods
    private static SecretKey generateTransactionkey() {
        try {
            KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
            aesKeyGen.init(256);
            return aesKeyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptographicOperationException("AES algorithm not available", e);
        }
    }

    private static SecretKey getAESKeyFromString(String base64Key) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64Key);
            return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        } catch (IllegalArgumentException e) {
            throw new CryptographicOperationException("Invalid Base64 AES key", e);
        }
    }

    private static void validateFileExists(String filePath) throws NoSuchFileException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new NoSuchFileException("File not found: " + filePath);
        }
        if (!Files.isReadable(path)) {
            throw new SecurityException("No read access to file: " + filePath);
        }
    }

    // Custom exceptions
    private static class CryptographicOperationException extends RuntimeException {
        public CryptographicOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class CryptographicInitializationException extends RuntimeException {
        public CryptographicInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}