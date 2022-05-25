package de.bjarnerest.asechat;

import de.bjarnerest.asechat.model.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
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

    void setTestModeMaxClients(@SuppressWarnings("SameParameterValue") int testModeMaxClients) {
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
            ChatRoomUserHandler userHandler = new ChatRoomUserHandler(this, serverSocket.accept());
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

    public void removeUserHandler(ChatRoomUserHandler handler) {
        this.userHandlers.remove(handler);
    }

    public boolean isProtected() {
        return this.password.isEmpty();
    }

    public boolean checkPassword(String passwordToCheck) {
        return passwordToCheck.equals(this.password);
    }

    public boolean isRunning() {
        return running;
    }
}
