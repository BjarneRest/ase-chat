package de.bjarnerest.asechat.server;

import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.instruction.BaseInstruction;
import de.bjarnerest.asechat.instruction.ChatChangeColorInstruction;
import de.bjarnerest.asechat.instruction.ChatLeaveInstruction;
import de.bjarnerest.asechat.instruction.ChatMessageEchoInstruction;
import de.bjarnerest.asechat.instruction.ChatMessageSendInstruction;
import de.bjarnerest.asechat.instruction.InstructionInvalidException;
import de.bjarnerest.asechat.instruction.SystemAuthenticateInstruction;
import de.bjarnerest.asechat.instruction.SystemErrorInstruction;
import de.bjarnerest.asechat.instruction.SystemReadyInstruction;
import de.bjarnerest.asechat.model.AnsiColor;
import de.bjarnerest.asechat.model.Message;
import de.bjarnerest.asechat.model.Station;
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
  
  private AnsiColor color = null;
  private final Socket clientSocket;
  boolean authenticated = false;
  private boolean left;
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

        this.executeInstruction(new SystemAuthenticateInstruction(Station.SERVER));

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
    this.executeInstruction(new SystemReadyInstruction(Station.SERVER));

  }

  private void handleMessage(@NotNull String line) {

    BaseInstruction instruction;

    try {

      instruction = InstructionNameHelper.parseInstruction(line, Station.CLIENT);

    } catch (InstructionInvalidException e) {

      logger.warning("Could not parse instruction " + e);
      this.executeInstruction(new SystemErrorInstruction(Station.SERVER, "parsing"));
      return;

    }

    if (!authenticated) {

      if (instruction instanceof SystemAuthenticateInstruction) {

        SystemAuthenticateInstruction authenticateInstruction = (SystemAuthenticateInstruction) instruction;

        logger.fine("Client tries to authenticate.");

        if (chatRoomServer.checkPassword(authenticateInstruction.getPassword())) {

          logger.fine("Password matched.");
          this.authenticate();
          return;

        } else {

          logger.fine("Password wrong.");

        }

      }

      this.executeInstruction(new SystemAuthenticateInstruction(Station.SERVER));

      return;
    }

    if (instruction instanceof ChatMessageSendInstruction) {

      ChatMessageSendInstruction chatMessageSendInstruction = (ChatMessageSendInstruction) instruction;

      // Client wants so send message
      logger.fine("Received message from client " + this.clientId);

      logger.fine("Message content: " + chatMessageSendInstruction.getMessage().toJson());

      if (color != null) {
        String messText = chatMessageSendInstruction.getMessage().getMessageText();
        chatMessageSendInstruction.getMessage().setMessageText(color.code + messText + AnsiColor.RESET.code);
      }
      chatRoomServer.publishMessage(chatMessageSendInstruction.getMessage(), this.clientId);
      this.executeInstruction(new ChatMessageEchoInstruction(Station.SERVER, chatMessageSendInstruction.getMessage()));

    } else if (instruction instanceof ChatLeaveInstruction) {

      logger.info("User left chatroom: " + this.clientId);
      this.left = true;

    } else if (instruction instanceof ChatChangeColorInstruction) {

      ChatChangeColorInstruction chatChangeColorInstruction = (ChatChangeColorInstruction) instruction;
      this.color = chatChangeColorInstruction.getColor();


    } else {

      logger.warning("Client (" + this.clientId + ") command cannot be interpreted: " + line);
      this.executeInstruction(new SystemErrorInstruction(Station.SERVER, "no_such_command"));

    }

  }

  public void executeInstruction(@NotNull BaseInstruction instruction) {

    this.out.println(instruction);

  }

  public void publishMessage(@NotNull Message message) {

    logger.fine("Publishing message: " + message.toJson());
    this.executeInstruction(new ChatMessageSendInstruction(Station.SERVER, message));

  }

  @NotNull
  public UUID getClientId() {
    return clientId;
  }
}
