package de.bjarnerest.asechat;

import picocli.CommandLine;

public class Main {

    public static void main(String[] args) {

        int exitCode = new CommandLine(new ChatManager()).execute(args);
        System.exit(exitCode);


    }

}
