package com.cougararray.RecDatabase;

public class RecordValue {
    private final ColumnName columnName;
    private final String value;

    public RecordValue(ColumnName columnName, String value) {
        this.columnName = columnName;
        this.value = value;
    }

    public String returnStatement() {
        return "SELECT IP_ADDRESS, NAME, PUBLICKEY FROM Users WHERE " + this.columnName.getkeyType() + " = '" + this.value + "';";
    }
}
