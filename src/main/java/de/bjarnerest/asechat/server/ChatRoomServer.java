package de.bjarnerest.asechat.server;

import de.bjarnerest.asechat.helper.HashingHelper;
import de.bjarnerest.asechat.model.Message;
import de.bjarnerest.asechat.model.PngImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

public class ChatRoomServer {

  final private InetAddress host;
  final private int port;
  final private String passwordHash;
  final private ArrayList<ChatRoomUserHandler> userHandlers;
  private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  protected ServerSocket serverSocket;
  private int testModeMaxClients = -1;
  private boolean running;

  public ChatRoomServer(InetAddress host, int port) {

    this(host, port, "");

  }

  public ChatRoomServer(InetAddress host, int port, String passwordHash) {

    this.host = host;
    this.port = port;
    this.passwordHash = passwordHash;
    this.userHandlers = new ArrayList<>();

  }

  public void setTestModeMaxClients(@SuppressWarnings("SameParameterValue") int testModeMaxClients) {

    logger.info("Enabling test mode. Refusing connections with more than " + testModeMaxClients + " clients");
    this.testModeMaxClients = testModeMaxClients;

  }

  protected void createSocket() throws IOException {

    this.serverSocket = new ServerSocket(this.port, 50, this.host);

  }

  public void startServer() throws IOException {

    if (this.running) {
      return;
    }

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

  public void publishMessage(Message message) {

    logger.info("Publishing message: " + message.toJson());
    logger.fine("Message sent by " + message.getMessageSender().getId());
    this.userHandlers.stream()
        .filter(userHandler -> userHandler.authenticated)
        .filter(userHandler -> !userHandler.getUserId().equals(message.getMessageSender().getId()))
        .forEach(userHandler -> userHandler.publishMessage(message));

  }

  public void publishPng(PngImage pngImage) {

    logger.info("Publishing png: " + pngImage.getFileName());
    logger.fine("Png sent by " + pngImage.getSender().getId());
    this.userHandlers.stream()
        .filter(userHandler -> userHandler.authenticated)
        .filter(userHandler -> !userHandler.getUserId().equals(pngImage.getSender().getId()))
        .forEach(userHandler -> userHandler.publishPng(pngImage));

  }

  public int getConnectedClientsAmount() {
    return this.userHandlers.size();
  }

  public void removeUserHandler(ChatRoomUserHandler handler) {

    this.userHandlers.remove(handler);

  }

  public boolean isProtected() {

    return !this.passwordHash.isEmpty();

  }

  public boolean checkPassword(String passwordToCheck) {

    return HashingHelper.verifySha512WithSalt(passwordToCheck, this.passwordHash);

  }

  public boolean isRunning() {
    return running;
  }
}
