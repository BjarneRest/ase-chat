package de.bjarnerest.asechat;

import de.bjarnerest.asechat.model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

public class ChatRoomServer {

    final private InetAddress host;
    final private int port;
    final private ArrayList<ChatRoomUserHandler> userHandlers;
    protected ServerSocket serverSocket;
    private int testModeMaxClients = -1;
    private boolean running;

    public ChatRoomServer(InetAddress host, int port) {
        this.host = host;
        this.port = port;
        this.userHandlers = new ArrayList<>();
    }

    void setTestModeMaxClients(int testModeMaxClients) {
        this.testModeMaxClients = testModeMaxClients;
    }

    protected void createSocket() throws IOException {
        this.serverSocket = new ServerSocket(this.port, 50, this.host);
    }

    public void startServer() throws IOException {
        if (this.running) return;

        this.running = true;
        this.createSocket();
        while (running && (this.testModeMaxClients == -1 || this.testModeMaxClients > this.userHandlers.size())) {
            ChatRoomUserHandler userHandler = new ChatRoomUserHandler(serverSocket.accept());
            userHandler.start();
            this.userHandlers.add(userHandler);
        }
        this.serverSocket.close();
    }

    public void publishMessage(Message message, UUID publisher) {
        for (ChatRoomUserHandler userHandler : userHandlers) {
            if (userHandler.getClientId().equals(publisher)) continue;
            userHandler.publishMessage(message);
        }
    }

    private class ChatRoomUserHandler extends Thread {

        private final UUID clientId;
        private final Socket clientSocket;
        private boolean left;
        private PrintWriter out;

        public ChatRoomUserHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.clientId = UUID.randomUUID();
        }

        @Override
        public void run() {

            try (
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {

                this.out = out;

                out.println("system:ready");

                String inLine;
                while (!left && ChatRoomServer.this.running && (inLine = in.readLine()) != null) {

                    handleMessage(inLine);

                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ChatRoomServer.this.userHandlers.remove(this);
                try {
                    this.clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        private void handleMessage(String line) {

            if (line.startsWith("chat:message:send=")) {
                // Client wants so send message
                // Try to form message object
                Message message = Message.fromJson(line.split("chat:message:send=", 2)[1]);
                ChatRoomServer.this.publishMessage(message, this.clientId);

            } else if (line.equals("chat:leave")) {
                this.left = true;
            } else {
                this.out.println("system:error");
            }

        }

        public void publishMessage(Message message) {
            this.out.println("chat:message:publish=" + message.toJson());
        }

        public UUID getClientId() {
            return clientId;
        }
    }


}
