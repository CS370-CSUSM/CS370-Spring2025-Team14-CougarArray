package com.cougararray.TCPWebsocket.Packets;

import org.json.JSONObject;

public class ResponsePacket {
    
    //status
    //0 = Successful
    //1 = Unsuccessful
    //2 = Unsuccessful & Error

    //{"CODE" : int, "COMMENT" : string}

    public ResponsePacket(int status, String output) {
    }

    public ResponsePacket success(String output) {
        return new ResponsePacket(0, output);
    }

    public ResponsePacket unsuccess(String output) {
        return new ResponsePacket(1, output);
    }

    public ResponsePacket error(String output) {
        return new ResponsePacket(2, output);
    }



    public static String toJson(int status) {
        JSONObject json = new JSONObject();
        json.put("CODE", status);
        json.put("type", "RESPONSE");
        return json.toString();
    }

    public static String toJson(int status, String comments) {
        JSONObject json = new JSONObject();
        json.put("CODE", status);
        json.put("COMMENT", comments);
        json.put("type", "RESPONSE");
        return json.toString();
    }
}
