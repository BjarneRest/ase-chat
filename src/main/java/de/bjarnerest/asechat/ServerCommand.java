package de.bjarnerest.asechat;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

@Command(name = "Server", version = "1.0", mixinStandardHelpOptions = true)
public class ServerCommand implements Runnable {

    @Option(names = {"-p", "--port"}, description = "Port number")
    int port = 25531;

    @Option(names = {"-h", "--host"}, description = "Host IP-Address")
    String hostIp = "0.0.0.0";

    @Option(names = {"-k", "--key", "--password"}, description = "Room password")
    String password = "";

    private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Override
    public void run() {

        logger.fine(this.getClass().getSimpleName() + " executed with arguments: this.port = " + this.port + "; this.hostIp = " + this.hostIp + "; this.password = " + this.password);

        try {
            InetAddress addr = InetAddress.getByName(this.hostIp);
            ChatRoomServer chatRoomServer = new ChatRoomServer(addr, this.port, this.password);
            logger.info("Starting server.");
            chatRoomServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
