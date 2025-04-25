package com.cougararray.TCPWebsocket.Packets;

import org.json.JSONObject;

public class PingPacket {

    public static String toJson() {
        JSONObject json = new JSONObject();
        json.put("type", "PING");
        return json.toString();
    }
    
}
