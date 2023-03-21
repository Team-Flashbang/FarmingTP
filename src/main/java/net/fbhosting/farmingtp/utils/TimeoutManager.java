package net.fbhosting.farmingtp.utils;

import java.util.Date;
import java.util.HashMap;

import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;

public class TimeoutManager {
  private HashMap<Player, Date> playerMap = new HashMap<>();
  
  @Getter @Setter
  private int requiredTimeout = 0;

  public TimeoutManager(int initialTimeout) {
    this.requiredTimeout = initialTimeout;
  }

  /**
   * Blocks the player by adding it to the list of blocked players.
   * @param player The player to block.
   */
  public void block(Player player) {
    this.playerMap.put(player, new Date());
  }

  /**
   * Checks if a player is blocked.
   * @param player The player to check.
   */
  public boolean isBlocked(Player player) {
    boolean isBlocked = this.getRemainingTime(player) > 0;
    
    // remove from memory if no longer blocked
    if (!isBlocked) this.playerMap.remove(player);

    return isBlocked;
  }

  /**
   * Returns the remaining time a player needs to wait in seconds, before the timeout is over.
   * @param player The player to check.
   */
  public long getRemainingTime(Player player) {
    Date playerDate = this.playerMap.get(player);

    // if no player is registered, just return 0
    if (playerDate == null) return 0;

    long dateDiff = (new Date().getTime() - playerDate.getTime()) / 1000;
    return this.requiredTimeout - dateDiff;
  }
}
