import java.io.*;
import java.net.*;

// ChatClient class to handle client-side operations
public class ChatClient {
    private Socket clientSocket; // Socket for connecting to the server
    private PrintWriter out; // Output stream to send data to the server
    private BufferedReader in; // Input stream to receive data from the server

    // Method to start connection to the server
    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port); // Create a socket to connect to the server
        out = new PrintWriter(clientSocket.getOutputStream(), true); // Initialize output stream
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Initialize input stream
        System.out.println("Connected to the chat server at " + ip + ":" + port);
    }

    // Method to send messages to the server
    public void sendMessage(String msg) {
        out.println(msg); // Send message to the server
    }

    // Method to receive messages from the server in a separate thread
    public void receiveMessages() {
        new Thread(() -> {
            String msg;
            try {
                while ((msg = in.readLine()) != null) {
                    System.out.println("Received: " + msg); // Print received message to console
                }
            } catch (IOException e) {
                e.printStackTrace(); // Handle exceptions
            }
        }).start();
    }

    // Method to stop the connection to the server
    public void stopConnection() throws IOException {
        in.close(); // Close input stream
        out.close(); // Close output stream
        clientSocket.close(); // Close client socket
    }

    // Main method to run the client
    public static void main(String[] args) throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in)); // Reader for user input
        ChatClient client = new ChatClient(); // Create a new client
        client.startConnection("127.0.0.1", 12345); // Connect to the server on localhost, port 12345
        client.receiveMessages(); // Start receiving messages from the server

        System.out.println("Connected to the chat server. Type your messages below:");

        String userInput;
        // Continuously read user input and send to the server
        while ((userInput = stdIn.readLine()) != null) {
            client.sendMessage(userInput); // Send user input to the server
        }
        client.stopConnection(); // Stop the connection
    }
}
