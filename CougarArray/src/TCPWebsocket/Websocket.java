package TCPWebsocket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import OutputT.Output;
import OutputT.Status;
import OutputT.Colors;



//A lot of the information founded for using websockets can be founded here: https://www.geeksforgeeks.org/java-net-socket-class-in-java/
//ServerSocket is for SERVER side tasks (for recieving stuff)
//Socket is for CLIENT side tasks (for sending stuff)
public class Websocket extends Thread{

    private int port;

    public Websocket(int port){
        this.port = port;
    }

    //extension of Thread
    public void run(){
        listen();
    }
    
    private void listen(){
        Output.print("Starting Websocket Reciever");

        //Connect to the port
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            Output.print("Server listening on port " + this.port + "...", Status.GOOD);

            //after successful connection, start listening for input messages
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Output.print("Client connected: " + clientSocket.getInetAddress(), Status.GOOD);

                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //this handles messages that are INPUTTED (ex. input would recieve information. If I send "Hello World" to a server, the server would save it here)
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true); //this handles what to write back (ex. if I recieve "Hello World", output would write back "How are you doing?")
                
                String received;
                while ((received = input.readLine()) != null) { //save input to the strong Recieived. If there are several lines on the strong; keep saving it until it's empty. (Similiar to reading a file with C++)
                    System.out.println("Received: " + received);
                    output.println("Message received: " + received);
                }
            }
        } catch (Exception e) {
            Output.print("Error Catched! Error revolves in Websocket.java!", Status.BAD);
            e.printStackTrace();
        }

        Output.print("Leaving Websocket Reciever...");
        
    }


    //To test Websocket; you can write Websocket.java itself and see if it can send a message to itself.
    //Ideally, if you are to execute Main.java, this code is NOT executed. If you executed Websocket.java then this code is executed.
    //This is helpful for testing purposes.

    //@TODO!
    //Better Logging; I feel like to a new user, they have 0 idea what any of this is...
    //Better documentation
    public static void main(String args[]) {
        int port = 6333; //@TODO! make it so, for testing purposes, user might want to use another port in case 6333 is used.
        
        Websocket websocket = new Websocket(port); 
        new Thread(() -> websocket.listen()).start(); //Concurrency is needed for websocket as it operates as another process

        try (Socket socket = new Socket("127.0.0.1", port);
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            while (true) {
                String message = System.console().readLine("Input a Message Here => ");
                
                if (message == null || message.equalsIgnoreCase("exit")) {
                    System.out.println("Closing connection...");
                    break;
                }

                output.println(message);

                String response = input.readLine();
                System.out.println("Server response: " + response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
