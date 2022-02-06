package de.bjarnerest.asechat;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "ChatManager", subcommands = {Server.class})
public class ChatManager {

    @Spec
    CommandSpec spec;


}
