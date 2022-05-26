package de.bjarnerest.asechat.client;

import de.bjarnerest.asechat.model.Message;
import de.bjarnerest.asechat.model.User;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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

        socket = new Socket(host, port);
        InputStream serverData = socket.getInputStream();
        clientDataOs = socket.getOutputStream();
        serverDataBuffered = new BufferedReader(new InputStreamReader(serverData));
        this.receiveMessage();

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
                Message message = new Message("Hello Welt. Hier ist " + user.getUsername() + "\n", user);
                this.sendLine("chat:message:send=" + message.toJson());
                System.out.println("Habs geschickt");
            }
            if (line.equals("system:authenticate")) {
                this.authenticate();
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

}
