package de.bjarnerest.asechat;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(name = "Client", version = "1.0", mixinStandardHelpOptions = true)
public class ClientCommand implements Runnable {

    @Option(names = {"-u", "--username"}, description = "Name of User")
    String username = "";

    @Override
    public void run() {

        System.out.println(username);

    }
}
