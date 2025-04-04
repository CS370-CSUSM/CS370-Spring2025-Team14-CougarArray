package com.cougararray.TCPWebsocket;

import java.util.Base64;

import org.json.JSONObject;

public class ContentPacket {
    private String fileName;
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private byte[] contentBase64; // Encrypted binary content

    public byte[] getContentBase64() {
        return contentBase64;
    }

    public void setContentBase64(byte[] contentBase64) {
        this.contentBase64 = contentBase64;
    }

    public ContentPacket(String fileName, byte[] contentBase64) {
        this.fileName = fileName;
        this.contentBase64 = contentBase64;
    }

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("fileName", fileName);
        json.put("content", Base64.getEncoder().encodeToString(contentBase64));
        return json.toString();
    }
}
