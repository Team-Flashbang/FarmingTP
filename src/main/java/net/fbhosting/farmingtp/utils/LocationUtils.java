package net.fbhosting.farmingtp.utils;

import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;

public class LocationUtils {
  private final Random random = new Random();

  // list of unsafe blocks
  // TODO: move this to configuration
  private final Set<Material> unsafeMaterials = Set.of(
    Material.LAVA,
    Material.WATER,
    Material.MAGMA_BLOCK,
    Material.CACTUS,
    Material.FIRE
  );

  public Location createSafeLocation(World world, int maxAttempts) {
    for (int i = 0; i < maxAttempts; i++) {
      Location location = this.getRandomLocation(world);

      // check if found safe location
      if (this.isLocationSafe(location)) return location;
    }

    // failed to find safe location
    return null;
  }

  public boolean isLocationSafe(Location location) {
    int x = location.getBlockX();
    int y = location.getBlockY();
    int z = location.getBlockZ();

    // get blocks
    Block below = location.getWorld().getBlockAt(x, y - 1, z);
    Block block = location.getBlock();
    Block above = location.getWorld().getBlockAt(x, y + 1, z);

    // dimension checks
    switch (location.getWorld().getEnvironment()) {
      case NORMAL:
        return !unsafeMaterials.contains(above.getType())
          && !unsafeMaterials.contains(block.getType())
          && !unsafeMaterials.contains(below.getType())
          && !above.isSolid()
          && !block.isSolid()
          && below.isSolid();

      case NETHER:
        return !unsafeMaterials.contains(above.getType())
          && !unsafeMaterials.contains(block.getType())
          && !unsafeMaterials.contains(below.getType())
          && !above.isSolid()
          && !block.isSolid()
          // && below.isSolid() // FIXME: why is this not working when enabled?
          && y < 126; // nether roof

      case THE_END:
        return !unsafeMaterials.contains(above.getType())
          && !unsafeMaterials.contains(block.getType())
          && !unsafeMaterials.contains(below.getType())
          && !above.isSolid()
          && !block.isSolid()
          && below.isSolid();
    
      default:
        return false;
    }
  }

  public Location getRandomLocation(World world) {
    double borderRadius = world.getWorldBorder().getSize() / 2;

    // set initial values
    int x = 0;
    int y = 0;
    int z = 0;

    // create a random x & z position
    x = this.random.nextInt((int)borderRadius);
    z = this.random.nextInt((int)borderRadius);

    // calc y
    if (world.getEnvironment() == Environment.NETHER) {
      // nether need special treatment
      y = getHighestBlockNether(world, x, z) + 1;
    }
    else {
      y = world.getHighestBlockYAt(x, z) + 1;
    }

    return new Location(world, x, y, z);
  }

  public int getHighestBlockNether(World world, int x, int z) {
    boolean scanForGround = false; // first scan for air, then for ground

    for (int y = 126; y > 0; y--) {
      Block currentBlock = world.getBlockAt(x, y, z);

      if (!scanForGround && currentBlock.isEmpty()) {
        // currently in air so switch to scanning for ground
        scanForGround = true;
      }
      else if (scanForGround &&  currentBlock.isSolid()) {
        return y + 1;
      }
    }

    return 100; // fallback value
  }
}
