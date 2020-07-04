package ru.prisonlife.pljobs;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.plugin.PLPlugin;
import ru.prisonlife.plugin.PromisedPluginFile;
import ru.prisonlife.pljobs.commands.*;
import ru.prisonlife.pljobs.events.*;

import java.io.File;
import java.util.*;

public class Main extends PLPlugin {

    public static Map<Player, Integer> playersSalary = new HashMap<>();

    public static List<Location> garbageChests = new ArrayList<>();
    public static Map<Integer, Location> cleanerPoints = new HashMap<>();
    public static Integer garbageCount = 0;
    public static BukkitTask taskGarbages;

    public static Map<String, Integer> minerTime = new HashMap<>();
    public static BukkitTask taskMine;

    @Override
    public String getPluginName() {
        return "PLJobs";
    }

    @Override
    public List<PromisedPluginFile> initPluginFiles() {
        return new ArrayList<>();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        copyConfigFile();
        registerCommands();
        registerListeners();
        loadInfo();
        getGarbagePoints();
        taskMine = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (String name : getConfig().getConfigurationSection("miners").getKeys(false)) {
                if (minerTime.containsKey(name)) {
                    minerTime.put(name, minerTime.get(name) + 1);
                }
            }
        }, 0, 20);
    }

    @Override
    public void onDisable() {
        Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
        if (!players.isEmpty()) {
            for (Player player : players) {
                Prisoner prisoner = PrisonLife.getPrisoner(player);
                if (prisoner.getJob() == Job.CLEANER) {
                    getConfig().set("jobs." + prisoner.getAccountNumber(), "cleaner");
                } else if (prisoner.getJob() == Job.MINER) {
                    getConfig().set("jobs." + prisoner.getAccountNumber(), "miner");
                } else if (prisoner.getJob() == Job.COOK) {
                    getConfig().set("jobs." + prisoner.getAccountNumber(), "cook");
                }
            }
        }

        if (!playersSalary.isEmpty()) {
            for (Player player : playersSalary.keySet()) {
                getConfig().set("salaries." + player.getName(), playersSalary.get(player));
            }
        }

        getConfig().set("garbages", garbageCount);

        if (!cleanerPoints.isEmpty()) {
            for (Integer point : cleanerPoints.keySet()) {
                World world = cleanerPoints.get(point).getWorld();
                int x = cleanerPoints.get(point).getBlockX();
                int y = cleanerPoints.get(point).getBlockY();
                int z = cleanerPoints.get(point).getBlockZ();

                getConfig().set("cleaners." + point.toString() + ".world", world.getName());
                getConfig().set("cleaners." + point.toString() + ".x", x);
                getConfig().set("cleaners." + point.toString() + ".y", y);
                getConfig().set("cleaners." + point.toString() + ".z", z);
            }
        }

        if (!garbageChests.isEmpty()) {
            for (Location garbage : garbageChests) {
                World world = garbage.getWorld();
                int x = garbage.getBlockX();
                int y = garbage.getBlockY();
                int z = garbage.getBlockZ();

                String name = String.format("%d&%d&%d", x, y, z);

                getConfig().set("garbageChests." + name + ".world", world.getName());
                getConfig().set("garbageChests." + name + ".x", x);
                getConfig().set("garbageChests." + name + ".y", y);
                getConfig().set("garbageChests." + name + ".z", z);
            }
        }

        saveConfig();
    }

    private void loadInfo() {
        ConfigurationSection section = getConfig().getConfigurationSection("jobs");
        if (section != null) {
            for (String number : section.getKeys(false)) {
                String string = getConfig().getString("jobs." + number);
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
        getConfig().set("jobs", null);
        if (getWorkerCount("cleaner") > 0) {
            taskGarbages = Bukkit.getScheduler().runTaskTimer(this, () -> {

                if (garbageCount < getWorkerCount("cleaner") * getConfig().getInt("cleaner.garbageCountPerCleaner")) {

                    List<Integer> list = new ArrayList<>();

                    for (Integer key : cleanerPoints.keySet()) {
                        list.add(key);
                    }

                    int rand = new Random().nextInt(list.size());

                    cleanerPoints.get(list.get(rand)).getWorld().dropItem(cleanerPoints.get(list.get(rand)), new ItemStack(Material.COCOA_BEANS, 1));
                    garbageCount ++;
                }
            }, 0, getConfig().getInt("cleaner.garbageSpawnIntensity") * 20);
        }

        section = getConfig().getConfigurationSection("salaries");
        if (section != null) {
            for (String nickname : section.getKeys(false)) {
                playersSalary.put(Bukkit.getPlayer(nickname), getConfig().getInt("salaries." + nickname));
            }
        }
        getConfig().set("salaries", null);

        section = getConfig().getConfigurationSection("garbages");
        if (section != null) {
            garbageCount = getConfig().getInt("garbages");
        }
        getConfig().set("garbages", null);

        section = getConfig().getConfigurationSection("garbageChests");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                World world = Bukkit.getWorld(getConfig().getString("garbageChests." + id + ".world"));
                int x = getConfig().getInt("garbageChests." + id + ".x");
                int y = getConfig().getInt("garbageChests." + id + ".y");
                int z = getConfig().getInt("garbageChests." + id + ".z");
                garbageChests.add(new Location(world, x, y, z));
            }
        }
        getConfig().set("garbageChests", null);

        section = getConfig().getConfigurationSection("cleaners");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                World world = Bukkit.getWorld(getConfig().getString("cleaners." + id + ".world"));
                int x = getConfig().getInt("cleaners." + id + ".x");
                int y = getConfig().getInt("cleaners." + id + ".y");
                int z = getConfig().getInt("cleaners." + id + ".z");

                cleanerPoints.put(Integer.parseInt(id), new Location(world, x, y, z));
            }
        }
        getConfig().set("garbageChests", null);

        saveConfig();
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

    public static Integer getWorkerCount(String job) {
        int count = 0;

        if (job.equals("cleaner")) {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (PrisonLife.getPrisoner(player).getJob() == Job.CLEANER) {
                    count++;
                }
            }
        }

        return count;
    }


}
