package de.bjarnerest.asechat.client;

import de.bjarnerest.asechat.model.Message;
import de.bjarnerest.asechat.model.User;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ChatRoomClient {

    final private InetAddress host;
    final private int port;
    final private String password;
    final private User user;
    private boolean authenticated = false;
    BufferedWriter clientDataBuffered;
    BufferedReader serverDataBuffered;

    public ChatRoomClient(InetAddress host, int port,  String password, String username) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.user = new User(username);
    }

    public void connectToServer() throws Exception {

        Socket socket = new Socket(host, port);
        InputStream serverData = socket.getInputStream();
        OutputStream clientData = socket.getOutputStream();
        clientDataBuffered = new BufferedWriter(new PrintWriter(clientData));
        serverDataBuffered = new BufferedReader(new InputStreamReader(serverData));
        this.recieveMessage();

    }

    public void sendMessage() throws Exception{
        clientDataBuffered.write("Hello");
    }

    public void recieveMessage() throws Exception {
        String line;
        while((line = serverDataBuffered.readLine()) != null) {
            System.out.println(line);
            if(line.equals("system:ready")) {
                authenticated = true;
                Message message = new Message("Hello Welt. Hier ist " + user.getUsername() + "\n", user);
                clientDataBuffered.write("chat:message:send=" + message.toJson() + "\n");
                System.out.println("Habs geschickt");
            }
            if (line.equals("system:authenticate")) {
                this.authenticate();
            }
        }
    }

    public void authenticate() throws Exception {
        clientDataBuffered.write("system:authenticate=" + password + "\r\n");
    }

}
