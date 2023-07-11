package hcmus.viet;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChatClient {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean isStarting;

    public ChatClient() {
        isStarting = false;
    }

    public boolean getState() {
        return isStarting;
    }

    public void startClient(String ip, int port) throws Exception {
        this.isStarting = true;
        // Establishing the connection
        socket = new Socket(ip, port);

        // Obtaining input and output streams
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());

        // Start a new thread for sending heartbeat messages
        Thread heartbeatThread = new Thread(new HeartbeatSender());
        heartbeatThread.start();
    }

    public void sendMessage(String message) {
        if (isStarting) {
            try {
                dos.writeUTF(message);
                if (message.equals("logout")) {
                    stopClient();
                }
            } catch (IOException e) {
                e.printStackTrace();
                stopClient();
            }
        }
    }

    public void readMessage(JTextArea textArea) {
        Thread readMessage = new Thread(() -> {
            while (isStarting) {
                try {
                    // Reading the message sent to this client
                    String msg = dis.readUTF();
                    System.out.println(msg);

                    if (msg.equals("heartbeat")) {
                        continue;
                    }

                    String[] line = msg.split("#");
                    int type = Integer.parseInt(line[0]);
                    String detail = line[1];
                    if (type == 1) {
                        Path path = Paths.get(detail);

                        if (Files.exists(path)) {
                            sendMessage("4#Path exists: " + detail);
                            textArea.append("Path exists: " + detail + "\n");
                            FolderMonitor monitor = new FolderMonitor(detail, this);
                            Thread threadMonitor = new Thread(monitor);
                            threadMonitor.start();
                        } else {
                            sendMessage("5#Path does not exist: " + detail);
                            textArea.append("Path does not exist: " + detail + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    stopClient();

                    try {
                        Thread.sleep(500); // Add a delay of 500 milliseconds before resetting the watch key
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        break;
                    }
                    ConnectPanel.showNotification("Server closed! Closing ...");
                    System.exit(0);
                }
            }
        });

        readMessage.start();
    }

    public void stopClient() {
        try {
            this.isStarting = false;
            // Closing resources
            if (dis != null)
                dis.close();
            if (dos != null)
                dos.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class HeartbeatSender implements Runnable {
        private static final int HEARTBEAT_INTERVAL = 5000; // Heartbeat interval in milliseconds

        @Override
        public void run() {
            try {
                while (isStarting) {
                    // Send heartbeat message to the server
                    dos.writeUTF("heartbeat");

                    // Sleep for the heartbeat interval
                    Thread.sleep(HEARTBEAT_INTERVAL);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                stopClient();
            }
        }
    }
}
