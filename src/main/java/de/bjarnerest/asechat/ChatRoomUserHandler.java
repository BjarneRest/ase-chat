package de.bjarnerest.asechat;

import com.google.gson.stream.MalformedJsonException;
import de.bjarnerest.asechat.model.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

class ChatRoomUserHandler extends Thread {

  private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private final ChatRoomServer chatRoomServer;
  private final UUID clientId;
  private final Socket clientSocket;
  private boolean left;
  boolean authenticated = false;
  private PrintWriter out;

  public ChatRoomUserHandler(ChatRoomServer chatRoomServer, Socket clientSocket) {
    this.chatRoomServer = chatRoomServer;
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
      if (!chatRoomServer.isProtected()) {
        this.authenticate();
      } else {
        this.println("system:authenticate");
      }

      while (!left && chatRoomServer.isRunning() && (inLine = in.readLine()) != null) {
        logger.finest("Received line: " + inLine);
        handleMessage(inLine);

      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      logger.info("Removing user handler for client " + this.clientId);
      chatRoomServer.removeUserHandler(this);
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
        if (chatRoomServer.checkPassword(passwordReceived)) {
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
        chatRoomServer.publishMessage(message, this.clientId);
        this.println("chat:message:echo=" + message.toJson());
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
