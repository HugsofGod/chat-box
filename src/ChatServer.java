import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

// ChatServer class to handle server-side operations
public class ChatServer {
    private ServerSocket serverSocket; // Server socket to listen for client connections
    private ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>(); // Thread-safe map to store clients

    // Constructor to initialize the server on a specific port
    public ChatServer(int port) throws IOException {
        serverSocket = new ServerSocket(port); // Create a server socket on the specified port
        System.out.println("Chat server started on port " + port);
    }

    // Method to start the server and accept client connections
    public void start() throws IOException {
        while (true) {
            // Accept new client connections and handle them in a new thread
            Socket clientSocket = serverSocket.accept(); // Accept an incoming client connection
            System.out.println("New client connected: " + clientSocket.getInetAddress());
            new ClientHandler(clientSocket, this).start(); // Start a new thread to handle the client
        }
    }

    // Method to broadcast messages to all connected clients
    public void broadcastMessage(String message, String sender) {
        for (ClientHandler client : clients.values()) {
            if (!client.getClientId().equals(sender)) { // Exclude the sender from receiving their own message
                client.sendMessage(message); // Send message to the client
            }
        }
    }

    // Method to add a client to the list of connected clients
    public void addClient(String clientId, ClientHandler clientHandler) {
        clients.put(clientId, clientHandler);
        System.out.println("Client " + clientId + " added.");
    }

    // Method to remove a client from the list of connected clients
    public void removeClient(String clientId) {
        clients.remove(clientId);
        System.out.println("Client " + clientId + " removed.");
    }

    // Main method to run the server
    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer(12345); // Create server on port 12345
        server.start(); // Start the server
    }
}

// ClientHandler class to manage communication with individual clients
class ClientHandler extends Thread {
    private Socket clientSocket; // Socket for the connected client
    private PrintWriter out; // Output stream to send data to the client
    private BufferedReader in; // Input stream to receive data from the client
    private ChatServer server; // Reference to the server
    private String clientId; // Unique ID for the client

    // Constructor to initialize the client handler with the client's socket and server reference
    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    // Run method to handle client communication
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true); // Initialize output stream
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Initialize input stream

            // Assign a unique ID to the client
            clientId = UUID.randomUUID().toString();
            server.addClient(clientId, this); // Add client to the server's client list

            String inputLine;
            // Continuously read messages from the client
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from " + clientId + ": " + inputLine); // Print received message to console
                server.broadcastMessage("Client " + clientId + ": " + inputLine, clientId); // Broadcast the message to other clients
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle exceptions
        } finally {
            try {
                server.removeClient(clientId); // Remove the client from the server's client list
                in.close(); // Close input stream
                out.close(); // Close output stream
                clientSocket.close(); // Close client socket
            } catch (IOException e) {
                e.printStackTrace(); // Handle exceptions
            }
        }
    }

    // Method to send messages to the client
    public void sendMessage(String msg) {
        out.println(msg); // Send message to the client
    }

    // Method to get the client's ID
    public String getClientId() {
        return clientId;
    }
}
