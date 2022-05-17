package de.bjarnerest.asechat;

import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(name = "ChatManager", subcommands = {ServerCommand.class, ClientCommand.class, HelpCommand.class})
public class ChatManager {

    @SuppressWarnings("unused")
    @Spec
    CommandSpec spec;

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    @Option(names = {"--config"}, description = "Path to config file. Defaults to ${DEFAULT-VALUE}")
    private String configFilePath = "config.properties";

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    @Option(names = {
        "--noconfig"}, description = "When set, no config file is created. The application will use default values, if no arguments are set.")
    private boolean ignoreConfig = false;


}
