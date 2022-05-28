package de.bjarnerest.asechat;

import de.bjarnerest.asechat.command.ConfigInitializerShadowCommand;
import java.io.OutputStream;
import java.io.PrintWriter;
import picocli.CommandLine;

public class Main {

  public static void main(String[] args) {

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
