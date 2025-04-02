package com.cougararray.RecDatabase;

public enum ColumnName {
    /*
     * Instead of the developer having to type "RED" or pulling the Colors Red, they can instead type the status
     */
    IP_ADDRESS("IP_ADDRESS"), 
    NAME("NAME"), 
    PUBLICKEY("PUBLICKEY");

    private final String type;
    private String value;

    ColumnName(String type) { 
        this.type = type;
    }

    public String getkeyType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }
}