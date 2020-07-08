package ru.prisonlife.pljobs;

import org.bukkit.configuration.file.FileConfiguration;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.garbageChests;

/**
 * @author rntsdkv
 * @project PLJobs
 */

public class GarbageChest {

    private PLPlugin plugin;
    public GarbageChest(PLPlugin plugin) {
        this.plugin = plugin;
    }

    private String world;
    private int x;
    private int y;
    private int z;

    public GarbageChest(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    public int getZ() {
        return z;
    }

    public void loadToConfig() {
        FileConfiguration config = plugin.getConfig();
        String world = getWorld();
        int x = getX();
        int y = getY();
        int z = getZ();

        config.set("garbageChests." + String.format("%d&%d&%d", x, y, z) + ".world", world);
        config.set("garbageChests." + String.format("%d&%d&%d", x, y, z) + ".x", x);
        config.set("garbageChests." + String.format("%d&%d&%d", x, y, z) + ".y", y);
        config.set("garbageChests." + String.format("%d&%d&%d", x, y, z) + ".z", z);

        plugin.saveConfig();
    }

    public boolean exists() {
        String world = getWorld();
        int x = getX();
        int y = getY();
        int z = getZ();

        boolean exist = false;
        for (GarbageChest garbageChest : garbageChests) {
            String gWorld = garbageChest.getWorld();
            int gX = garbageChest.getX();
            int gY = garbageChest.getY();
            int gZ = garbageChest.getZ();

            if (world.equals(gWorld) && x == gX && y == gY && z == gZ) {
                exist = true;
                break;
            }
        }
        if (!exist) return false;
        return true;
    }
}
