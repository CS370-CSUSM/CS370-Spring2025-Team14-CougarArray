package com.cougararray.Cryptography;

public class CryptographyResult {
    public byte[] encryptedData;
    public boolean successful;

    public CryptographyResult() {
        this.encryptedData = null;
        this.successful = false;
    }

    public CryptographyResult(byte[] encryptedData, boolean resultOfOperation) {
        this.encryptedData = encryptedData;
        this.successful = resultOfOperation;
    }

    public boolean CryptographyResult() {
        return this.successful;
    }

    public boolean successful() {
        return this.successful;
    }
}
