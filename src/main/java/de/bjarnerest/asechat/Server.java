package de.bjarnerest.asechat;

import picocli.CommandLine;

@CommandLine.Command(name = "Server", version = "1.0", mixinStandardHelpOptions = true)
public class Server implements Runnable {

    @CommandLine.Option(names = {"-p", "--port"}, description = "Port number")
    int port = 25531;

    @CommandLine.Option(names = {"-h", "--host"}, description = "Host IP-Address")
    String hostIp = "0.0.0.0";

    @Override
    public void run() {

        System.out.println("this.port = " + this.port);
        System.out.println("this.hostIp = " + this.hostIp);

    }
}
