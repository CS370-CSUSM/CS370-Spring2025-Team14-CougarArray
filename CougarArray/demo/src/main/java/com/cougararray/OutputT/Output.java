package com.cougararray.OutputT;

public class Output {
    
    public static void print(String message, Status status) {
        String[] lines = message.split("\n");
        for (String line : lines) {
            System.out.println(status.OutputCode() + " " + line);
        }
    }

    //Allow the user NOT to put in a status code; make this OK then
    //Note: Java doesn't support default parameters, so you have to make use of overloading
    public static void print(String message) {
        print(message, Status.DASH);
    }

    public static void print(Object obj) {
        print(obj.toString(), Status.DASH);
    }

    //ErrorPrint returns false because some code has it so it outputs something AND returns false to imply
    //unsuccessful execution of code
    //this functions merges the two
    public static boolean errorPrint(String message) {
        print(message, Status.BAD);
        return false;
    }

    // Inline print option
    public static void printInline(String message, Status status) {
        String[] lines = message.split("\n");
        for (int i = 0; i < lines.length; i++) {
            System.out.print(status.OutputCode() + " " + lines[i]);
            if (i < lines.length - 1) {
                System.out.print("\n"); // keep newlines between lines
            }
        }
    }

    public static void printInline(String message) {
        printInline(message, Status.OK);
    }

    public static void printInline(Object obj) {
        printInline(obj.toString(), Status.OK);
    }


    public static void printStackTrace(Exception e, Status status) {
        String[] lines = e.toString().split("\n");
        for (String line : lines) {
            System.out.println(status.OutputCode() + " " + line);
        }
        e.printStackTrace(); // prints the stack trace to the console
    }

    public static void printStackTrace(Exception e) {
        printStackTrace(e, Status.BAD); // Use BAD status by default
    }
    public static void main(String[] args){
        print("Testing for Output.java");
        
        for (Status s : Status.values()) {
            print("Test", s);
        }
    }
}
