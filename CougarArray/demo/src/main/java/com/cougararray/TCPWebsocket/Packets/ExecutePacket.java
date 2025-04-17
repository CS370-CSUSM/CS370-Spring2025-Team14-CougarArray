package com.cougararray.TCPWebsocket.Packets;

import org.json.JSONObject;

public class ExecutePacket {

    private String command;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("type", "EXECUTE");
        json.put("execute", getCommand());
        return json.toString();
    }

}
