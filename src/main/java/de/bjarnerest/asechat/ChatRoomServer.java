package de.bjarnerest.asechat;

import com.google.gson.stream.MalformedJsonException;
import de.bjarnerest.asechat.model.Message;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public class ChatRoomServer {

    final private InetAddress host;
    final private int port;
    final private String password;
    final private ArrayList<ChatRoomUserHandler> userHandlers;
    private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    protected ServerSocket serverSocket;
    private int testModeMaxClients = -1;
    private boolean running;

    public ChatRoomServer(InetAddress host, int port) {
        this(host, port, "");
    }

    public ChatRoomServer(InetAddress host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.userHandlers = new ArrayList<>();
    }

    void setTestModeMaxClients(int testModeMaxClients) {
        logger.info("Enabling test mode. Refusing connections with more than " + testModeMaxClients + " clients");
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
            logger.fine("Waiting for new client to connect.");
            ChatRoomUserHandler userHandler = new ChatRoomUserHandler(serverSocket.accept());
            logger.info("New client socket connected. Starting user handler thread.");
            userHandler.start();
            this.userHandlers.add(userHandler);
        }
        this.serverSocket.close();
    }

    public void publishMessage(Message message, UUID publisher) {
        logger.info("Publishing message: " + message.toJson());
        logger.fine("Message sent by " + publisher);
        this.userHandlers.stream()
                .filter(userHandler -> userHandler.authenticated)
                .filter(userHandler -> !userHandler.getClientId().equals(publisher))
                .forEach(userHandler -> userHandler.publishMessage(message));
    }

    private class ChatRoomUserHandler extends Thread {

        private final UUID clientId;
        private final Socket clientSocket;
        private boolean left;
        private boolean authenticated = false;
        private PrintWriter out;

        public ChatRoomUserHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.clientId = UUID.randomUUID();
            logger.fine("Assigned clientId " + this.clientId);
        }

        @Override
        public void run() {

            try (
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {

                this.out = out;
                String inLine;
                if (ChatRoomServer.this.password.isEmpty()) {
                    this.authenticate();
                } else {
                    this.println("system:authenticate");
                }


                while (!left && ChatRoomServer.this.running && (inLine = in.readLine()) != null) {
                    logger.finest("Received line: " + inLine);
                    handleMessage(inLine);

                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                logger.info("Removing user handler for client " + this.clientId);
                ChatRoomServer.this.userHandlers.remove(this);
                try {
                    this.clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        private void authenticate() {
            this.authenticated = true;
            logger.fine("Greeting new client with system:ready");
            this.println("system:ready");
        }

        private void handleMessage(@NotNull String line) {

            if (!authenticated) {

                if (line.startsWith("system:authenticate=")) {

                    logger.fine("Client tries to authenticate.");
                    String passwordReceived = line.split("system:authenticate=", 2)[1];
                    if (ChatRoomServer.this.password.equals(passwordReceived)) {
                        logger.fine("Password matched.");
                        this.authenticate();
                        return;
                    } else {
                        logger.fine("Password wrong.");
                    }

                }

                this.println("system:authenticate");

                return;
            }

            if (line.startsWith("chat:message:send=")) {
                // Client wants so send message
                // Try to form message object
                logger.fine("Received message from client " + this.clientId);
                logger.finest("Trying to deserialize message json");

                try {
                    Message message = Message.fromJson(line.split("chat:message:send=", 2)[1]);
                    logger.fine("Message content: " + message.toJson());
                    ChatRoomServer.this.publishMessage(message, this.clientId);
                } catch (MalformedJsonException e) {
                    logger.severe(e.toString());
                    this.println("system:error:parsing");
                }

            } else if (line.equals("chat:leave")) {
                logger.info("User left chatroom: " + this.clientId);
                this.left = true;
            } else {
                logger.warning("Client (" + this.clientId + ") command cannot be interpreted: " + line);
                this.println("system:error:no_such_command");
            }

        }

        private synchronized void println(String line) {
            this.out.println(line);
        }

        public void publishMessage(@NotNull Message message) {
            logger.fine("Publishing message: " + message.toJson());
            this.println("chat:message:publish=" + message.toJson());
        }

        @NotNull
        public UUID getClientId() {
            return clientId;
        }
    }


}
