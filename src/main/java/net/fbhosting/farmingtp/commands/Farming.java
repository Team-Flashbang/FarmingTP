package net.fbhosting.farmingtp.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;

import lombok.extern.slf4j.Slf4j;
import net.fbhosting.farmingtp.FarmingTP;
import net.fbhosting.farmingtp.utils.LocationUtils;
import net.fbhosting.farmingtp.utils.TimeoutManager;

@Slf4j
public class Farming implements CommandExecutor, TabCompleter {
  private final MultiverseCore multiverseCore = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
  private final MVWorldManager worldManager = this.multiverseCore.getMVWorldManager();
  private final SafeTTeleporter teleporter = this.multiverseCore.getSafeTTeleporter();
  private final LocationUtils utils = new LocationUtils();
  private final JavaPlugin plugin = FarmingTP.getInstance();
  private final FileConfiguration config = this.plugin.getConfig();
  private final TimeoutManager timeoutManager = new TimeoutManager(plugin.getConfig().getInt("general.timeout"));

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args) {
    // only allow commands from players
    if (!(sender instanceof Player)) return false;
    Player player = (Player) sender;

    // permission check
    if (!player.hasPermission(command.getPermission())) {
      player.sendMessage(this.config.getString("locale.missingPermission", "You are lacking the required permissions."));
      return true;
    }

    // check if player is in timeout
    if (!player.hasPermission("farmingtp.bypass") && this.timeoutManager.isBlocked(player)) {
      String timeoutMessage = this.config.getString("locale.timeout", "You need to wait {seconds} seconds before using this command again.");
      player.sendMessage(timeoutMessage.replace("{seconds}", Long.toString(this.timeoutManager.getRemainingTime(player))));
      return true;
    }
    this.timeoutManager.block(player);

    // get default value and possible values
    List<String> possibleKeys = this.plugin.getConfig().getConfigurationSection("general.worlds").getKeys(true).stream().toList();
    String defaultValue = possibleKeys.get(0);

    // make sure the user has given a valid argument
    if (args.length > 0 && !possibleKeys.contains(args[0])) {
      player.sendMessage(this.config.getString("locale.invalidArgument", "Sorry, but you specified an invalid argument."));
      return true;
    }

    // get user selected world
    String worldName = args.length > 0 ? this.plugin.getConfig().getString("general.worlds." + args[0]) : this.plugin.getConfig().getString("general.worlds." + defaultValue);
    MultiverseWorld world = this.worldManager.getMVWorld(worldName);

    // check if world could be found
    if (world == null) {
      player.sendMessage(this.config.getString("locale.worldNotFound", "Failed to find world. Please contact a server administrator."));
      log.error("{} tried to teleport to world {}, but the world does not exist. Please check your configuration.", player.getName(), worldName);
      return true;
    }

    // prevent server crash and make sure world is loaded
    this.worldManager.loadWorld(worldName);

    // find location
    int maxAttempts = this.plugin.getConfig().getInt("general.maxTries");
    Location location = utils.createSafeLocation(world.getCBWorld(), maxAttempts);
    if (location == null) {
      player.sendMessage(this.config.getString("locale.noSafeLocation", "Failed to find a safe location. Please try again."));
      return true;
    }

    // teleport player
    player.sendMessage(this.config.getString("locale.teleport", "Teleporting..."));
    if (!location.isChunkLoaded()) world.getCBWorld().loadChunk(location.getChunk());
    this.teleporter.safelyTeleport(sender, player, location, false);

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args) {
    return this.plugin.getConfig().getConfigurationSection("general.worlds").getKeys(true).stream().toList();
  }
  
}
