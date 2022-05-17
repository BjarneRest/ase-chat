package de.bjarnerest.asechat;

import java.io.OutputStream;
import java.io.PrintWriter;
import picocli.CommandLine;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {

        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setLevel(Level.ALL);
        logger.getParent().getHandlers()[0].setLevel(Level.ALL);

        // Init config
        CommandLine configShadowCli = new CommandLine(new ConfigInitializerShadowCommand());
        configShadowCli.setOut(new PrintWriter(OutputStream.nullOutputStream()));
        configShadowCli.setUnmatchedOptionsArePositionalParams(true);
        configShadowCli.execute(args);


        // Run main application
        int exitCode = new CommandLine(new ChatManager()).execute(args);
        System.exit(exitCode);


    }

}
