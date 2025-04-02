/*
- privateKey: String
- publicKey: String
- File: File

+setKeys()
+getKeys()
+getFile(String)
*/

package com.cougararray.Cryptography;

public class Cryptography {
    protected Keys keys;

    //this needs to be consistent when using encryption methods...using AES and RSA combined can cause tomfoolery
    protected static final String algorithm = "RSA";

    public Cryptography(String privateKey, String publicKey) {
        this.keys = new Keys(privateKey, publicKey);
    }
}