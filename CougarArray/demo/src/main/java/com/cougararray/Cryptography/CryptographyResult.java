package com.cougararray.Cryptography;

public class CryptographyResult {
    public byte[] encryptedData;
    public boolean successful;
    public byte[] encryptedKey;

    public CryptographyResult() {
        this.encryptedData = null;
        this.successful = false;
        this.encryptedKey = null;
    }

    public CryptographyResult(byte[] encryptedData, boolean resultOfOperation) {
        this.encryptedData = encryptedData;
        this.successful = resultOfOperation;
    }

    public CryptographyResult(byte[] encryptedData, boolean resultOfOperation, byte[] encryptedKey) {
        this.encryptedData = encryptedData;
        this.successful = resultOfOperation;
        this.encryptedKey = encryptedKey;
    }

    public boolean successful() {
        return this.successful;
    }
}
