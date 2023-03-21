package net.fbhosting.farmingtp.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;

import lombok.extern.slf4j.Slf4j;
import net.fbhosting.farmingtp.utils.LocationUtils;
import net.fbhosting.farmingtp.utils.TimeoutManager;

@Slf4j
public class Farming implements CommandExecutor, TabCompleter {
  private final MultiverseCore multiverseCore = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
  private final MVWorldManager worldManager = multiverseCore.getMVWorldManager();
  private final SafeTTeleporter teleporter = multiverseCore.getSafeTTeleporter();
  private final LocationUtils utils = new LocationUtils();
  private final TimeoutManager timeoutManager;
  private final JavaPlugin plugin;

  public Farming(JavaPlugin plugin) {
    this.timeoutManager = new TimeoutManager(plugin.getConfig().getInt("general.timeout"));
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args) {
    // only allow commands from players
    if (!(sender instanceof Player)) return false;
    Player player = (Player) sender;

    // permission check
    if (!player.hasPermission(command.getPermission())) {
      player.sendMessage("You are lacking the required permissions.");
      return true;
    }

    // check if player is in timeout
    if (!player.hasPermission("farmingtp.bypass") && this.timeoutManager.isBlocked(player)) {
      player.sendMessage("You need to wait " + this.timeoutManager.getRemainingTime(player) + " before using this command again.");
      return true;
    }
    this.timeoutManager.block(player);

    // get default value and possible values
    List<String> possibleKeys = this.plugin.getConfig().getConfigurationSection("worlds").getKeys(true).stream().toList();
    String defaultValue = possibleKeys.get(0);

    // make sure the user has given a valid argument
    if (args.length > 0 && !possibleKeys.contains(args[0])) {
      player.sendMessage("Sorry, but you specified an invalid argument.");
      return true;
    }

    // get user selected world
    String worldName = args.length > 0 ? this.plugin.getConfig().getString("worlds." + args[0]) : this.plugin.getConfig().getString("worlds." + defaultValue);
    MultiverseWorld world = this.worldManager.getMVWorld(worldName);

    // check if world could be found
    if (world == null) {
      player.sendMessage("Failed to find world. Please contact a server administrator.");
      log.error("{} tried to teleport to world {}, but the world does not exist. Please check your configuration.", player.getName(), worldName);
      return true;
    }

    // prevent server crash and make sure world is loaded
    this.worldManager.loadWorld(worldName);

    // find location
    int maxAttempts = this.plugin.getConfig().getInt("general.maxTries");
    Location location = utils.createSafeLocation(world.getCBWorld(), maxAttempts);
    if (location == null) {
      player.sendMessage("Failed to find a safe location. Please try again.");
      return true;
    }

    // teleport player
    player.sendMessage("Teleporting...");
    if (!location.isChunkLoaded()) world.getCBWorld().loadChunk(location.getChunk());
    this.teleporter.safelyTeleport(sender, player, location, false);

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args) {
    return this.plugin.getConfig().getConfigurationSection("worlds").getKeys(true).stream().toList();
  }
  
}
