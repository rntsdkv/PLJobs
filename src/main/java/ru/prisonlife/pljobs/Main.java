package ru.prisonlife.pljobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.plugin.PLPlugin;
import ru.prisonlife.util.Pair;
import ru.prisonlife.pljobs.commands.*;
import ru.prisonlife.pljobs.events.*;

import java.io.File;
import java.util.*;

public class Main extends PLPlugin {

    public static Map<Player, Integer> playersSalary = new HashMap<>();
    public static Map<Integer, Location> cleanerPoints = new HashMap<>();
    public static Integer garbageCount = 0;
    public static BukkitTask task;

    public String getPluginName() {
        return "PLJobs";
    }

    public List<Pair<String, Object>> initPluginFiles() {
        return null;
    }

    public void onCreate() {
        copyConfigFile();
    }

    public void onEnable() {
        registerCommands();
        registerListeners();
        getGarbagePoints();
    }

    private void registerCommands() {
        getCommand("cleanersetpoint").setExecutor(new CleanerSetPoint(this));
        getCommand("cleanerdelpoint").setExecutor(new CleanerDeletePoint(this));
        getCommand("cleanerlist").setExecutor(new CleanerList(this));
        getCommand("getsalary").setExecutor(new GetSalary(this));
        getCommand("gowork").setExecutor(new GoWork(this));
        getCommand("setgarbage").setExecutor(new SetGarbage(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ChestClick(this), this);
        getServer().getPluginManager().registerEvents(new ChestRemove(this), this);
        getServer().getPluginManager().registerEvents(new GarbagePickup(this), this);
        getServer().getPluginManager().registerEvents(new GarbageRemove(this), this);
        getServer().getPluginManager().registerEvents(new GarbageThrowAway(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClick(this), this);
        getServer().getPluginManager().registerEvents(new ItemsDrop(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLeave(this), this);
    }

    private void copyConfigFile() {
        File config = new File(getDataFolder() + File.separator + "config.yml");
        if (!config.exists()) {
            getLogger().info("PLJobs | Default Config copying...");
            getConfig().options().copyDefaults(true);
            saveDefaultConfig();
        }
    }

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void getGarbagePoints() {
        for (String id : getConfig().getConfigurationSection("cleaners").getKeys(false)) {

            String world = getConfig().getString("cleaners." + id + ".world");
            int x = getConfig().getInt("cleaners." + id + ".x");
            int y = getConfig().getInt("cleaners." + id + ".y");
            int z = getConfig().getInt("cleaners." + id + ".z");

            cleanerPoints.put(Integer.parseInt(id), new Location(Bukkit.getWorld(world), x, y, z));
        }
    }

    public static Integer getCleanersCount() {
        int count = 0;

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (PrisonLife.getPrisoner(player).getJob() == Job.CLEANER) {
                count ++;
            }
        }

        return count;
    }


}
