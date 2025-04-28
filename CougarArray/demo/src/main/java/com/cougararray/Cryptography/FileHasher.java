package com.cougararray.Cryptography;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

// I google "file hashing java" to learn about it and AI just gave me a majority of baseline code
// edited a few small things to make more sense
// can probably just use this in CentralMGMTEngine and compare hashes before/after send, not sure best way
public class FileHasher {

    /**
     * SHA-256 hash of a file
     *
     * @param filePath path to the file
     * @return hex-encoded SHA-256 hash
     * @throws Exception if the file cannot be read or hashed
     */
    public static String hashFile(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        File file = new File(filePath);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        byte[] bytes = digest.digest();
        return bytesToHex(bytes);
    }

    /**
     * SHA-256 hash of a byte array (is this part even necessary?)
     *
     * @param data data to hash
     * @return hex-encoded SHA-256 hash
     * @throws Exception if the hashing algorithm is unavailable
     */
    public static String hashBytes(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}