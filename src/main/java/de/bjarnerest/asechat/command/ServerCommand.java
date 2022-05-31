package de.bjarnerest.asechat.command;

import de.bjarnerest.asechat.helper.ConfigHelper;
import de.bjarnerest.asechat.helper.HashingHelper;
import de.bjarnerest.asechat.server.ChatRoomServer;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Server", version = "1.0", mixinStandardHelpOptions = true)
public class ServerCommand implements Runnable {

  private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  @Option(names = {"-p", "--port"}, description = "Port number")
  int port = ConfigHelper.getInstance().getConfig().getInt("server.port");
  @Option(names = {"-h", "--host"}, description = "Host IP-Address")
  String hostIp = ConfigHelper.getInstance().getConfig().getString("server.host");
  @Option(names = {"-k", "--key", "--password"}, description = "Room password")
  String password = ConfigHelper.getInstance().getConfig().getString("server.password");

  @Override
  public void run() {

    logger.fine(this.getClass().getSimpleName() + " executed with arguments: this.port = " + this.port + "; this.hostIp = "
        + this.hostIp + "; this.password = " + this.password);

    try {
      InetAddress addr = InetAddress.getByName(this.hostIp);
      String hashedPassword = this.password.isEmpty() ? "" : HashingHelper.hashSha512WithSalt(this.password);
      ChatRoomServer chatRoomServer = new ChatRoomServer(addr, this.port, hashedPassword);
      logger.info("Starting server.");
      logger.info("Server running at " + this.hostIp + ":" + this.port);
      chatRoomServer.startServer();
    }
    catch (IOException e) {
      e.printStackTrace();
    }


  }
}
