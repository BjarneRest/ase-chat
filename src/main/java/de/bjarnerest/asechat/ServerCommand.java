package de.bjarnerest.asechat;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.net.InetAddress;

@Command(name = "Server", version = "1.0", mixinStandardHelpOptions = true)
public class ServerCommand implements Runnable {

    @Option(names = {"-p", "--port"}, description = "Port number")
    int port = 25531;

    @Option(names = {"-h", "--host"}, description = "Host IP-Address")
    String hostIp = "0.0.0.0";

    @Option(names = {"-k", "--key", "--password"}, description = "Room password")
    String password = "";

    @Override
    public void run() {

        System.out.println("this.port = " + this.port);
        System.out.println("this.hostIp = " + this.hostIp);
        System.out.println("this.password = " + this.password);

        try {
            InetAddress addr = InetAddress.getByName(this.hostIp);
            ChatRoomServer chatRoomServer = new ChatRoomServer(addr, this.port);
            chatRoomServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
