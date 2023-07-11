package hcmus.viet;

import java.io.*;
import java.net.Socket;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

class ClientHandler implements Runnable {
    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isLogged;

    public boolean isExists;

    private List<Object> messageLog;

    private Instant  lastActivityTime;
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(10);

    private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(5);


    // constructor
    public ClientHandler(Socket s, String name,
                         DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.messageLog = new ArrayList<>();
        this.isLogged = true;
        this.isExists = false;
    }

    public String getName() {
        return name;
    }

    public List<Object> getMessageLog() {
        return messageLog;
    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        boolean isConnectedAdded = false; // Flag to track if "CONNECTED" message is already added
        String received;

        while (!s.isClosed()) {
            try {
                LocalDate currentDate = LocalDate.now();
                LocalTime currentTime = LocalTime.now();
                // Format the date
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String formattedDate = currentDate.format(dateFormatter);

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String formattedTime = currentTime.format(timeFormatter);
                String operation = null;

                if (!isConnectedAdded) {
                    Object[] connected = {formattedDate, formattedTime, "CONNECTED", "New client connected!"};
                    messageLog.add(connected);
                    isConnectedAdded = true;
                }

                // receive the string
                received = dis.readUTF();

                System.out.println("RECEIVED: " + this.name + ": " + received);

                if (received.equals("logout") || s.isClosed()) {
                    MainFrame.removeRow(MainFrame.clientModel, this.name);
                    System.out.println("This client has been logged out: " + s);
                    this.isLogged = false;
                    this.s.close();
                    break;
                }

                if (received.equals("heartbeat")) {
                    updateLastActivityTime();
                    continue;
                }

                // break the string into message and recipient part
                StringTokenizer st = new StringTokenizer(received, "#");
                int type = Integer.parseInt(st.nextToken());
                String message = st.nextToken();

                switch (type) {
                    case 1: // create
                        operation = "CREATE";
                        break;
                    case 2: // delete
                        operation = "DELETE";
                        break;
                    case 3: // modify
                        operation = "MODIFY";
                        break;
                    case 4: // folder exists
                        operation = "EXIST";
                        isExists = true;
                        break;
                    case 5: // folder doesnt exist
                        operation = "NOT EXIST";
                        isExists = false;
                        break;
                }
                Object[] line = {formattedDate, formattedTime, operation, message};
                messageLog.add(line);

            } catch (IOException e) {
                if (e.getMessage().equals("Connection reset")) {
                    break;
                } else {
                    e.printStackTrace();
                }
            }
            // Check connection timeout
            if (isConnectionTimeout()) {
                // Handle the case when connection timeout occurs
                // For example, you can consider the client as disconnected and take appropriate actions

                MainFrame.removeRow(MainFrame.clientModel, this.name);
                System.out.println("Connection timeout. Client disconnected: " + s);
                this.isLogged = false;

                try {
                    this.s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }

        }
        try {
            // closing resources
            this.dis.close();
            this.dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateLastActivityTime() {
        lastActivityTime = Instant.now();
    }

    private synchronized boolean isConnectionTimeout() {
        Instant expirationTime = lastActivityTime.plus(CONNECTION_TIMEOUT);
        return Instant.now().isAfter(expirationTime);
    }

}