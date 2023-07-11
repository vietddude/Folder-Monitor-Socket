package hcmus.viet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {
    private final String ip;
    private final int port;
    private ServerSocket serverSocket;
    private boolean isRunning;
    static List<ClientHandler> clientHandlerList;

    public Server(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.isRunning = false;
        clientHandlerList = new ArrayList<>();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("Server started on " + ip + ":" + port);

            while (isRunning) {
                System.out.println("Waiting for clients to connect...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Create a new client handler for the connected client
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                String clientName = "Client " + clientHandlerList.size();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientName, dis, dos);
                clientHandlerList.add(clientHandler);
                MainFrame.clientModel.addRow(new String[]{clientName});

                // Handle client connection in a separate thread
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            isRunning = false;
            if (serverSocket != null) {
                serverSocket.close();
                System.out.println("Server stopped");
            }
        } catch (IOException e) {
            System.err.println("Error stopping the server: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        start();
    }
}
