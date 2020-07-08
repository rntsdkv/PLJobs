package ru.prisonlife.pljobs;

import org.bukkit.configuration.file.FileConfiguration;
import ru.prisonlife.plugin.PLPlugin;

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

    public void loadToConfig(GarbageChest garbage) {
        FileConfiguration config = plugin.getConfig();
        String world = garbage.getWorld();
        int x = garbage.getX();
        int y = garbage.getY();
        int z = garbage.getZ();

        config.set("garbageChests." + String.format("%d&%d&%d", x, y, z) + ".world", world);
        config.set("garbageChests." + String.format("%d&%d&%d", x, y, z) + ".x", x);
        config.set("garbageChests." + String.format("%d&%d&%d", x, y, z) + ".y", y);
        config.set("garbageChests." + String.format("%d&%d&%d", x, y, z) + ".z", z);

        plugin.saveConfig();
    }
}
