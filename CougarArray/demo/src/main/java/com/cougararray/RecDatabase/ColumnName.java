// ColumnName.java
package com.cougararray.RecDatabase;

public enum ColumnName {
    IP_ADDRESS("IP_ADDRESS"),
    PORT      ("PORT"),
    NAME      ("NAME"),
    PUBLICKEY ("PUBLICKEY");

    private final String type;
    private String       value;

    ColumnName(String type) {
        this.type = type;
    }

    public String getkeyType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
