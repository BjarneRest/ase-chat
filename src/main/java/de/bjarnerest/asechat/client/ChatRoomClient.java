package de.bjarnerest.asechat.client;

import de.bjarnerest.asechat.helper.InstructionNameHelper;
import de.bjarnerest.asechat.instruction.*;
import de.bjarnerest.asechat.model.AnsiColor;
import de.bjarnerest.asechat.model.Message;
import de.bjarnerest.asechat.model.Station;
import de.bjarnerest.asechat.model.User;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChatRoomClient {

  final private InetAddress host;
  final private int port;
  final private String password;
  final private User user;
  OutputStream clientDataOs;
  BufferedReader serverDataBuffered;
  private Socket socket;
  private boolean authenticated = false;

  public ChatRoomClient(InetAddress host, int port, String password, String username, AnsiColor color) {
    this.host = host;
    this.port = port;
    this.password = password;
    this.user = new User(username, color);
  }

  public ChatRoomClient(InetAddress host, int port, String password, String username) {
    this.host = host;
    this.port = port;
    this.password = password;
    this.user = new User(username);
  }

  public void connectToServer() throws Exception {

    socket = createSocket();
    InputStream serverData = socket.getInputStream();
    clientDataOs = socket.getOutputStream();
    serverDataBuffered = new BufferedReader(new InputStreamReader(serverData));
    this.receiveMessage();

  }

  protected Socket createSocket() throws Exception {
    return new Socket(host, port);
  }

  public void receiveMessage() throws Exception {
    String line;
    while ((line = serverDataBuffered.readLine()) != null) {

      BaseInstruction instruction = InstructionNameHelper.parseInstruction(line, Station.SERVER);

      if (instruction instanceof SystemReadyInstruction) {
        SystemReadyInstruction castInstruction = (SystemReadyInstruction) instruction;
        handleInstruction(castInstruction);
      }
      else if (instruction instanceof SystemAuthenticateInstruction) {
        SystemAuthenticateInstruction castInstruction = (SystemAuthenticateInstruction) instruction;
        handleInstruction(castInstruction);
      }
      else if (instruction instanceof ChatMessageSendInstruction) {
        ChatMessageSendInstruction castInstruction = (ChatMessageSendInstruction) instruction;
        handleInstruction(castInstruction);
      }
      else if (instruction instanceof ChatInfoInstruction) {
        ChatInfoInstruction castInstruction = (ChatInfoInstruction) instruction;
        handleInstruction(castInstruction);
      } else if (instruction instanceof ChangeUserInstruction) {
        ChangeUserInstruction castInstruction = (ChangeUserInstruction) instruction;
        handleInstruction(castInstruction);
      }

    }
  }

  public void handleInstruction (SystemReadyInstruction instruction) throws Exception {
    authenticated = true;
    Message message = new Message("Hallo Welt. Hier ist " + user.getUsername(), user);
    this.sendInstruction(new ChatMessageSendInstruction(Station.CLIENT, message));
    this.sendInstruction(new ChatInfoInstruction(Station.CLIENT));
    handleUserInput();
  }

  public void handleInstruction (ChangeUserInstruction instruction) throws Exception {
    this.sendInstruction(new ChangeUserInstruction(Station.CLIENT, user));
  }

  public void handleInstruction (SystemAuthenticateInstruction instruction) throws Exception {
    this.authenticate();
  }

  public void handleInstruction (ChatMessageSendInstruction chatMessageSendInstruction) throws Exception {
    Message message = chatMessageSendInstruction.getMessage();
    User messageSender = message.getMessageSender();
    getUserOutputStream().printf("\n%s%s%s: %s\n>>> ", messageSender.getColor().code, messageSender.getUsername(), AnsiColor.RESET.code, message.getMessageText());
  }

  public void handleInstruction (ChatInfoInstruction chatInfoInstruction) throws Exception {

    AnsiColor color = AnsiColor.RED;
    getUserOutputStream().printf(
            "\n%sAktuell befinden sich %s Clients im Chatraum.%s\n>>>",
            color.code,
            chatInfoInstruction.getConnectedClientsAmount(),
            AnsiColor.RESET.code
    );
  }

  public void authenticate() throws Exception {
    this.sendInstruction(new SystemAuthenticateInstruction(Station.CLIENT, password));
  }

  private void sendInstruction(BaseInstruction instruction) throws Exception {
    this.sendLine(instruction.toString());
  }

  private void sendLine(String line) throws Exception {
    //getUserOutputStream().println("send line = " + line);
    this.clientDataOs.write(line.getBytes(StandardCharsets.UTF_8));
    this.clientDataOs.write("\n".getBytes(StandardCharsets.UTF_8));
  }

  protected InputStream getUserInputStream() {
    return System.in;
  }

  protected PrintStream getUserOutputStream() {
    return System.out;
  }

  public void handleUserInput() {
    Thread userThread = new Thread(new Runnable() {
      @Override
      public void run() {
        String line;
        Scanner scanner = new Scanner(getUserInputStream());
        getUserOutputStream().print("\n>>> ");
        while ((line = scanner.nextLine()) != null) {
          getUserOutputStream().print("\n>>> ");

          if (line.startsWith("/")) {

            if (line.equals("/leave") || line.equals("/quit")) {
              try {
                sendInstruction(new ChatLeaveInstruction(Station.CLIENT));
                socket.close();
                scanner.close();
              }
              catch (Exception e) {
                throw new RuntimeException(e);
              }
              return;
            }
            else if (line.startsWith("/color message ") || line.startsWith("/color username ")) {

              String[] split = line.split(" ");
              String colorStr = split[2].toUpperCase();
              AnsiColor color = user.getColor();
              try {
                color = AnsiColor.valueOf(colorStr);
              } catch (IllegalArgumentException ex) {
                getUserOutputStream().print("\nThis is not a valid color.\n>>>");
              }


              if(split[1].equals("username")) {
                user.setColor(color);
                try {
                  sendInstruction(new ChangeUserInstruction(Station.CLIENT, user));
                  printName();
                }
                catch (Exception e) {
                  e.printStackTrace();
                }
                continue;
              }

              ChatChangeColorInstruction instruction = new ChatChangeColorInstruction(Station.CLIENT, color);
              try {
                sendInstruction(instruction);
                getUserOutputStream().printf("\nYour messages are now %s%s%s\n>>>", color.code, color, AnsiColor.RESET.code);
              }
              catch (Exception e) {
                throw new RuntimeException(e);
              }


            }
            else if(line.equals("/info")) {

              ChatInfoInstruction instruction = new ChatInfoInstruction(Station.CLIENT);
              try {
                sendInstruction(instruction);
              }
              catch (Exception e) {
                throw new RuntimeException(e);
              }

            }
            else if (line.startsWith("/username ")) {
              String[] split = line.split(" ");
              user.setUsername(split[1]);
              try {
                sendInstruction(new ChangeUserInstruction(Station.CLIENT, user));
                printName();
              }
              catch (Exception e) {
                e.printStackTrace();
              }
            }

            continue;
          }

          Message message = new Message(line, user);

          try {
            sendInstruction(new ChatMessageSendInstruction(Station.CLIENT, message));
          }
          catch (Exception e) {
            e.printStackTrace();
          }

        }
      }
    });

    userThread.start();

  }

  private void printName() {
    getUserOutputStream().printf("\nYour name has been set to %s%s%s\n>>>", user.getColor().code, user.getUsername(), AnsiColor.RESET.code);
  }

}
