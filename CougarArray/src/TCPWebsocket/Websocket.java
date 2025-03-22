package TCPWebsocket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

//A lot of the information founded for using websockets can be founded here: https://www.geeksforgeeks.org/java-net-socket-class-in-java/
//ServerSocket is for SERVER side tasks (for recieving stuff)
//Socket is for CLIENT side tasks (for sending stuff)
public class Websocket {
    
    private void listen(int port){
        //Connect to the port
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port + "...");

            //after successful connection, start listening for input messages
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //this handles messages that are INPUTTED (ex. input would recieve information. If I send "Hello World" to a server, the server would save it here)
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true); //this handles what to write back (ex. if I recieve "Hello World", output would write back "How are you doing?")
                
                String received;
                while ((received = input.readLine()) != null) { //save input to the strong Recieived. If there are several lines on the strong; keep saving it until it's empty. (Similiar to reading a file with C++)
                    System.out.println("Received: " + received);
                    output.println("Message received: " + received);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //To test Websocket; you can write Websocket.java itself and see if it can send a message to itself.
    //Ideally, if you are to execute Main.java, this code is NOT executed. If you executed Websocket.java then this code is executed.
    //This is helpful for testing purposes.

    //@TODO!
    //Better Logging; I feel like to a new user, they have 0 idea what any of this is...
    //Better documentation
    public static void main(String args[]) {
        int port = 6333; //@TODO! make it so, for testing purposes, user might want to use another port in case 6333 is used.
        
        Websocket websocket = new Websocket(); 
        new Thread(() -> websocket.listen(port)).start(); //Concurrency is needed for websocket as it operates as another process

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
