package OutputT;

public class Output {
    
    public static void print(String message, Status status) {
        System.out.println(status.OutputCode() + " " + message);
    }

    //Allow the user NOT to put in a status code; make this OK then
    //Note: Java doesn't support default parameters, so you have to make use of overloading
    public static void print(String message) {
        print(message, Status.OK);
    }

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
