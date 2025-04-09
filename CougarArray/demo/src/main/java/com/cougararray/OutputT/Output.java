package com.cougararray.OutputT;

public class Output {
    
    public static void print(String message, Status status) {
        System.out.println(status.OutputCode() + " " + message);
    }

    //Allow the user NOT to put in a status code; make this OK then
    //Note: Java doesn't support default parameters, so you have to make use of overloading
    public static void print(String message) {
        print(message, Status.OK);
    }

    public static void print(Object obj) {
        print(obj.toString(), Status.OK);
    }

    //ErrorPrint returns false because some code has it so it outputs something AND returns false to imply
    //unsuccessful execution of code
    //this functions merges the two
    public static boolean errorPrint(String message) {
        print(message, Status.BAD);
        return false;
    }

    public static void main(String[] args){
        print("Testing for Output.java");
        
        for (Status s : Status.values()) {
            print("Test", s);
        }
    }
}
