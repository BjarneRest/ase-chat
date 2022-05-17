package de.bjarnerest.asechat;

import de.bjarnerest.asechat.helper.ConfigHelper;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command
public class ConfigInitializerShadowCommand implements Runnable {

  @SuppressWarnings("unused")
  @Parameters
  String[] remainder;

  @SuppressWarnings("FieldMayBeFinal")
  @Option(names = {"--config"}, description = "Path to config file. Defaults to ${DEFAULT-VALUE}")
  private String configFilePath = "config.properties";

  @SuppressWarnings("FieldMayBeFinal")
  @Option(names = {
      "--noconfig"}, description = "When set, no config file is created. The application will use default values, if no arguments are set.")
  private boolean ignoreConfig = false;

  @Override
  public void run() {
    if (ignoreConfig) {
      ConfigHelper.getInstance().initTemp();
    } else {
      ConfigHelper.getInstance().init(configFilePath);
    }
  }

}
