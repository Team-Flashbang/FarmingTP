package net.fbhosting.farmingtp;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.fbhosting.farmingtp.commands.Farming;

@Slf4j
public final class FarmingTP extends JavaPlugin implements Listener {
  @Getter
  private static FarmingTP instance;

  @Override
  public void onEnable() {
    instance = this; // NOSONAR - intended

    // check dependencies
    if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") == null) {
      log.error("Multiverse-Core not found! You are missing a dependency. Please install Multiverse-Core.");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    // load default configuration
    saveDefaultConfig();

    // register commands
    getCommand("farming").setPermission("farmingtp.teleport");
    getCommand("farming").setExecutor(new Farming());;
  }

  @Override
  public void onDisable() {
    log.info("Plugin disabled.");
  }
} 