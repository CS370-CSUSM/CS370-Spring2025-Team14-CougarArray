/*
+ decryptLocally()
+ decryptWithOutsideKey(String)
*/

package Cryptography;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.crypto.Cipher;

public class Decrypytion extends Cryptography {
    public Decrypytion(String privateKey, String publicKey) {
        super(privateKey, publicKey);
    }
    
    public boolean Decrypt(String Filepath, String output) throws Exception {
        try {
            byte[] fileData = Files.readAllBytes(Paths.get(Filepath + ".enc"));
            byte[] decryptedData = decryptContent(fileData);

            Files.write(Paths.get(output), decryptedData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private byte[] decryptContent(byte[] content) throws Exception {
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, this.keys.getPrivateKeyFromString());
        return cipher.doFinal(content);
    }
}