package com.cougararray.OutputT;

public enum Status {
    /*
     * Instead of the developer having to type "RED" or pulling the Colors Red, they can instead type the status
     */
    OK(Colors.DARK_GRAY), 
    BAD(Colors.RED), 
    GOOD(Colors.GREEN),
    DASH(Colors.DARK_GRAY),
    NONE(Colors.NONE);

    private final String colorCode;

    Status(String colorCode) { 
        this.colorCode = colorCode;
    }

    private String getColorCode() {
        return colorCode;
    }

    private String getStatusName() {
    return this == DASH ? "-" : this.name();
}

public String OutputCode() {
    if (this == NONE) {
        return "";
    } else {
        return "[" + this.getColorCode() + this.getStatusName() + Colors.RESET + "]";
    }
}
}