package ru.prisonlife.pljobs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
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
    public static BukkitTask taskMine;

    public String getPluginName() {
        return "PLJobs";
    }

    public List<Pair<String, Object>> initPluginFiles() {
        return null;
    }

    @Override
    public void onEnable() {
        copyConfigFile();
        registerCommands();
        registerListeners();
        resetAfterReload();
        getGarbagePoints();
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Prisoner prisoner = PrisonLife.getPrisoner(player);
            if (prisoner.getJob() == Job.CLEANER) {
                getConfig().set("save.jobs." + prisoner.getAccountNumber(), "cleaner");
            } else if (prisoner.getJob() == Job.MINER) {
                getConfig().set("save.jobs." + prisoner.getAccountNumber(), "miner");
            } else if (prisoner.getJob() == Job.COOK) {
                getConfig().set("save.jobs." + prisoner.getAccountNumber(), "cook");
            }
        }

        for (Player player : playersSalary.keySet()) {
            getConfig().set("save.salaries." + player.getName(), playersSalary.get(player));
        }

        getConfig().set("save.garbages", garbageCount);

        saveConfig();
    }

    private void resetAfterReload() {
        if (getConfig().getConfigurationSection("save") != null) {
            ConfigurationSection section = getConfig().getConfigurationSection("save.jobs");
            if (section != null) {
                for (String number : section.getKeys(false)) {
                    String string = getConfig().getString("save.jobs." + number);
                    int account = Integer.parseInt(number);
                    if (string.equals("cleaner")) {
                        PrisonLife.getPrisoner(account).setJob(Job.CLEANER);
                    } else if (string.equals("miner")) {
                        PrisonLife.getPrisoner(account).setJob(Job.MINER);
                    } else if (string.equals("cook")) {
                        PrisonLife.getPrisoner(account).setJob(Job.COOK);
                    }

                }
            }

            section = getConfig().getConfigurationSection("save.salaries");
            if (section != null) {
                for (String nickname : section.getKeys(false)) {
                    playersSalary.put(Bukkit.getPlayer(nickname), getConfig().getInt("save.salaries." + nickname));
                }
            }

            section = getConfig().getConfigurationSection("save.garbages");
            if (section != null) {
                garbageCount = getConfig().getInt("save.garbages");
            }

            getConfig().set("save", null);
            saveConfig();
        }
    }

    private void registerCommands() {
        getCommand("cleanersetpoint").setExecutor(new CleanerSetPoint(this));
        getCommand("cleanerdelpoint").setExecutor(new CleanerDeletePoint(this));
        getCommand("cleanerlist").setExecutor(new CleanerList(this));
        getCommand("getsalary").setExecutor(new GetSalary(this));
        getCommand("gowork").setExecutor(new GoWork(this));
        getCommand("setgarbage").setExecutor(new SetGarbage(this));
        getCommand("miner").setExecutor(new Miner(this));
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ChestCleanerListener(this), this);
        pluginManager.registerEvents(new GarbageListener(this), this);
        pluginManager.registerEvents(new JobGuiListener(this), this);
        pluginManager.registerEvents(new ItemsDrop(), this);
        pluginManager.registerEvents(new WorkerListener(), this);
    }

    private void copyConfigFile() {
        File config = new File(getDataFolder() + File.separator + "config.yml");
        if (!config.exists()) {
            getLogger().info("Default config copying...");
            getConfig().options().copyDefaults(true);
            saveDefaultConfig();
            getLogger().info("Config copied...");
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
