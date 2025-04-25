package com.cougararray.TCPWebsocket.Packets;

import java.util.Base64;

import com.cougararray.OutputT.Output;

import org.json.JSONObject;

public class ContentPacket {
    private String fileName;
    private byte[] contentBase64; // Encrypted binary content
    private byte[] key;
    
    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getContentBase64() {
        return contentBase64;
    }

    public void setContentBase64(byte[] contentBase64) {
        this.contentBase64 = contentBase64;
    }

    public ContentPacket(String fileName, byte[] contentBase64, byte[] key) {
        this.fileName = fileName;
        this.contentBase64 = contentBase64;
        this.key = key;
    }

    public ContentPacket(String rawJson) {
        JSONObject json = new JSONObject(rawJson);
        this.fileName = json.getString("fileName");
        this.contentBase64 = Base64.getDecoder().decode(json.getString("content"));
        this.key = Base64.getDecoder().decode(json.getString("key"));
    }
    

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("type", "CONTENT");
        json.put("fileName", fileName);
        json.put("content", Base64.getEncoder().encodeToString(contentBase64));
        json.put("key", Base64.getEncoder().encodeToString(key));
        return json.toString();
    }
    
    //update this example code
    public static void main(String[] args) {
        // Simulate original packet
        ContentPacket original = new ContentPacket("example.txt", new byte[]{10, 20, 30}, null);
        String json = original.toJson();
        Output.print("JSON: " + json);

        // Deserialize
        ContentPacket recovered = new ContentPacket(json);
        Output.print("Recovered filename: " + recovered.getFileName());
        Output.print("Recovered content: " + java.util.Arrays.toString(recovered.getContentBase64()));
    }
}
