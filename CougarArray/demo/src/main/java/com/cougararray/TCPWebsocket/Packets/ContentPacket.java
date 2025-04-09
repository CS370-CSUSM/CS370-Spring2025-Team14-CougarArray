package com.cougararray.TCPWebsocket.Packets;

import java.util.Base64;

import javax.swing.text.AbstractDocument.Content;

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

    public ContentPacket(String rawJson) {
        JSONObject json = new JSONObject(rawJson);
        this.fileName = json.getString("fileName");
        this.contentBase64 = Base64.getDecoder().decode(json.getString("content"));
    }
    

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("type", "CONTENT");
        json.put("fileName", fileName);
        json.put("content", Base64.getEncoder().encodeToString(contentBase64));
        return json.toString();
    }
    
    public static void main(String[] args) {
        // Simulate original packet
        ContentPacket original = new ContentPacket("example.txt", new byte[]{10, 20, 30});
        String json = original.toJson();
        System.out.println("JSON: " + json);

        // Deserialize
        ContentPacket recovered = new ContentPacket(json);
        System.out.println("Recovered filename: " + recovered.getFileName());
        System.out.println("Recovered content: " + java.util.Arrays.toString(recovered.getContentBase64()));
    }
}
