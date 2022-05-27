package de.bjarnerest.asechat.client;

import de.bjarnerest.asechat.model.Message;
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

  public void sendMessage() throws Exception {
    this.sendLine("Hello");
  }

  public void receiveMessage() throws Exception {
    String line;
    while ((line = serverDataBuffered.readLine()) != null) {
      //getUserOutputStream().println(line);
      if (line.equals("system:ready")) {
        authenticated = true;
        Message message = new Message("Hello Welt. Hier ist " + user.getUsername(), user);
        this.sendLine("chat:message:send=" + message.toJson());
        handleUserInput();
      } else if (line.equals("system:authenticate")) {
        this.authenticate();
      } else if (line.startsWith("chat:message:send")) {
        String messageJson = line.split("=")[1];
        Message message = Message.fromJson(messageJson);
        getUserOutputStream().printf("\n%s: %s\n>>> ", message.getMessageSender().getUsername(), message.getMessageText());
      }
    }
  }

  public void authenticate() throws Exception {
    this.sendLine("system:authenticate=" + password);
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
                sendLine("chat:leave");
                socket.close();
                scanner.close();
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }

            return;
          }

          Message message = new Message(line, user);

          try {
            sendLine("chat:message:send=" + message.toJson());
          } catch (Exception e) {
            e.printStackTrace();
          }

        }
      }
    });

    userThread.start();

  }

}
