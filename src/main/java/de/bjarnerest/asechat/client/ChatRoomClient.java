package de.bjarnerest.asechat.client;

import de.bjarnerest.asechat.model.Message;
import de.bjarnerest.asechat.model.User;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChatRoomClient {

    final private InetAddress host;
    final private int port;
    final private String password;
    final private User user;
    private Socket socket;
    private boolean authenticated = false;
    OutputStream clientDataOs;
    BufferedReader serverDataBuffered;

    public ChatRoomClient(InetAddress host, int port,  String password, String username) {
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

    protected Socket createSocket() throws Exception{
        return new Socket(host, port);
    }

    public void sendMessage() throws Exception{
        this.sendLine("Hello");
    }

    public void receiveMessage() throws Exception {
        String line;
        while((line = serverDataBuffered.readLine()) != null) {
            System.out.println(line);
            if(line.equals("system:ready")) {
                authenticated = true;
                Message message = new Message("Hello Welt. Hier ist " + user.getUsername(), user);
                this.sendLine("chat:message:send=" + message.toJson());
                handleUserInput();
            }
            if (line.equals("system:authenticate")) {
                this.authenticate();
            }
            if (line.startsWith("chat:message:send")) {
                String messageJson = line.split("=")[1];
                Message message = Message.fromJson(messageJson);
                System.out.printf("%s: %s\n", message.getMessageSender().getUsername(), message.getMessageText());
            }
        }
    }

    public void authenticate() throws Exception {
        this.sendLine("system:authenticate=" + password);
    }

    private void sendLine(String line) throws Exception {
        System.out.println("send line = " + line);
        this.clientDataOs.write(line.getBytes(StandardCharsets.UTF_8));
        this.clientDataOs.write("\n".getBytes(StandardCharsets.UTF_8));
    }

    public void handleUserInput () {
        Thread userThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String line;
                Scanner scanner = new Scanner(System.in);
                scanner.nextLine();
                while ((line = scanner.nextLine()) != null) {
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
