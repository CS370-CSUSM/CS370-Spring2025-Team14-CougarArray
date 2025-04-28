// RecordValue.java
package com.cougararray.RecDatabase;

public class RecordValue {
    private final ColumnName columnName;
    private final String     value;

    public RecordValue(ColumnName columnName, String value) {
        this.columnName = columnName;
        this.value      = value.replaceAll("\\s", "");
    }

    public String returnStatement() {
        return "SELECT IP_ADDRESS, PORT, NAME, PUBLICKEY"
             + " FROM Users WHERE "
             + columnName.getkeyType()
             + " = '" + value + "';";
    }
}
