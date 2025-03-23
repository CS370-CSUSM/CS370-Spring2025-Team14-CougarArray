package Output;

enum Status {
    /*
     * Instead of the developer having to type "RED" or pulling the Colors Red, they can instead type the status
     */
    OK(Colors.DARK_GRAY), 
    BAD(Colors.RED), 
    GOOD(Colors.GREEN);

    private final String colorCode;

    Status(String colorCode) { 
        this.colorCode = colorCode;
    }

    private String getColorCode() {
        return colorCode;
    }

    private String getStatusName() {
        return this.name();
    }

    public String OutputCode() {
        String output = Colors.RESET + "[" + this.getColorCode() + this.getStatusName() + Colors.RESET + "]";
        return output;
    }
}

public class Colors {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String DARK_GRAY = "\u001B[90m"; 
}
