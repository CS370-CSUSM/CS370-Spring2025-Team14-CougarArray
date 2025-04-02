package com.cougararray.RecDatabase;

public enum keyType {
    /*
     * Instead of the developer having to type "RED" or pulling the Colors Red, they can instead type the status
     */
    IP_ADDRESS("IP_ADDRESS"), 
    NAME("NAME"), 
    PUBLICKEY("PUBLICKEY");

    private final String type;

    keyType(String type) { 
        this.type = type;
    }

    private String getkeyType() {
        return this.type;
    }

    public String returnStatement(String input) {
        return "SELECT IP_ADDRESS, NAME, PUBLICKEY FROM Users WHERE " + getkeyType() + " = '" + input + "';";
    }
}