package de.bjarnerest.asechat.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jetbrains.annotations.NotNull;

public class ConfigHelper {

  private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  private static ConfigHelper instance;
  private Configuration config;

  private ConfigHelper() {
  }

  public static ConfigHelper getInstance() {
    if (instance == null) {
      instance = new ConfigHelper();
    }
    return instance;
  }

  public boolean isReady() {
    return config != null;
  }

  public void initTemp() {

    try {
      Path tempFilePath = Files.createTempFile("hill", ".properties");
      File tempFile = new File(String.valueOf(tempFilePath));
      Configurations configs = new Configurations();
      logger.fine("Generating temp config");
      try {
        this.config = generateDefaultConfigFile(configs, tempFile);
      } catch (Exception e) {
        logger.severe("Error while generating default config" + e);
      }

      logger.fine("Temp config file: " + tempFile);
    } catch (IOException e) {
      logger.severe("Could not create temp config" + e);
    }

  }

  public void init(String configFilePath) {

    Configurations configs = new Configurations();
    File configFile = new File(configFilePath);

    if (configFile.exists()) {

      try {
        this.config = configs.properties(configFile);
      } catch (ConfigurationException e) {
        logger.severe("Error while reading config" + e);
      }

    } else {

      logger.info("Generating default config");
      try {
        Files.createFile(configFile.toPath());
        this.config = generateDefaultConfigFile(configs, configFile);
      } catch (Exception e) {
        logger.severe("Error while generating default config" + e);
      }

    }

  }

  @NotNull
  private Configuration generateDefaultConfigFile(@NotNull Configurations configs, @NotNull File file)
      throws ConfigurationException, IOException {

    FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.propertiesBuilder(file);
    PropertiesConfiguration newConfig = builder.getConfiguration();

    // Default config
    newConfig.addProperty("server.port", 25531);
    newConfig.addProperty("server.host", "0.0.0.0");
    newConfig.addProperty("server.password", "");
    newConfig.addProperty("client.port", 25531);
    newConfig.addProperty("client.host", "127.0.0.1");
    newConfig.addProperty("client.password", "");
    newConfig.addProperty("client.username", "Bob");

    builder.save();

    return newConfig;

  }

  public Configuration getConfig() {
    return config;
  }

  public static ConfigHelper forceNewInstance() {
    instance = new ConfigHelper();
    return instance;
  }

}
