package de.bjarnerest.asechat;

import de.bjarnerest.asechat.helper.ConfigHelper;
import java.util.logging.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(name = "Client", version = "1.0", mixinStandardHelpOptions = true)
public class ClientCommand implements Runnable {

    private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Option(names = {"-p", "--port"}, description = "Port number")
    int port = ConfigHelper.getInstance().getConfig().getInt("server.port");

    @Option(names = {"-h", "--host"}, description = "Host IP-Address")
    String hostIp = ConfigHelper.getInstance().getConfig().getString("server.host");

    @Option(names = {"-u", "--username"}, description = "Name of User", required = true)
    String username = ConfigHelper.getInstance().getConfig().getString("client.username");

    @Override
    public void run() {

        logger.finest("Username: " + username);
        logger.finest("hostIp: " + hostIp);
        logger.finest("port: " + port);

    }
}
