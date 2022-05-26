package de.bjarnerest.asechat.command;

import de.bjarnerest.asechat.client.ChatRoomClient;
import de.bjarnerest.asechat.helper.ConfigHelper;

import java.net.InetAddress;
import java.util.logging.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(name = "Client", version = "1.0", mixinStandardHelpOptions = true)
public class ClientCommand implements Runnable {

  private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  @Option(names = {"-p", "--port"}, description = "Port number")
  int port = ConfigHelper.getInstance().getConfig().getInt("client.port");

  @Option(names = {"-h", "--host"}, description = "Host IP-Address")
  String hostIp = ConfigHelper.getInstance().getConfig().getString("client.host");

  @Option(names = {"-k", "--key", "--password"}, description = "Password")
  String password = ConfigHelper.getInstance().getConfig().getString("client.password");

  @Option(names = {"-u", "--username"}, description = "Name of User")
  String username = ConfigHelper.getInstance().getConfig().getString("client.username");

  @Override
  public void run() {

    logger.finest("Username: " + username);
    logger.finest("hostIp: " + hostIp);
    logger.finest("port: " + port);

    try {
      ChatRoomClient client = new ChatRoomClient(InetAddress.getByName(hostIp), port, password, username);
      client.connectToServer();
    }
    catch(Exception e) {

    }



  }
}
