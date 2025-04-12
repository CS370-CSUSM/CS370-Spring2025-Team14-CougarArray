package com.cougararray.TCPWebsocket.Packets;

import org.json.JSONObject;

public class ClosePacket {
        public static String toJson() {
        JSONObject json = new JSONObject();
        json.put("type", "CLOSE");
        return json.toString();
    }
}
