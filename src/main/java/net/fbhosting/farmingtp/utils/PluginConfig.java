package net.fbhosting.farmingtp.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginConfig extends YamlConfiguration {
  
  public PluginConfig(Path filePath) {
    super();

    // check if file exists
    if (!filePath.toFile().exists()) {
      try {
        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);
      }
      catch (IOException e) {
        log.error("Failed to create empty configuration file: {}", e);
      }
    }

    // load configuration
    try {
      this.load(filePath.toFile());
    }
    catch (IOException | InvalidConfigurationException e) {
      log.error("Failed to read configuration: {}", e);
      throw new RuntimeException(e);
    }
  }
}
