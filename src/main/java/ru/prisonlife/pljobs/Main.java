package ru.prisonlife.pljobs;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
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
    public static Map<String, Integer> minerBlockValues = new HashMap<>();
    public static BukkitTask taskMine;

    public static List<Location> orePoints = new ArrayList<>();
    public static List<Location> oreStorages = new ArrayList<>();
    public static List<Location> ironStorages = new ArrayList<>();

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
            for (String name : minerTime.keySet()) {
                minerTime.put(name, minerTime.get(name) + 1);

                if (minerTime.get(name) == getConfig().getInt("miners." + name + ".time") - 5) {
                    sendAlarmMiners("soon", name);
                } else if (minerTime.get(name) >= getConfig().getInt("miners." + name + ".time")) {
                    minerTime.replace(name, 0);
                    sendAlarmMiners("reload", name);

                    mineReset(name);
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
        pluginManager.registerEvents(new MinerListener(this), this);
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

    private void sendAlarmMiners(String type, String mineName) {
        for (Player player : getServer().getOnlinePlayers()) {
            Prisoner prisoner = PrisonLife.getPrisoner(player);
            if (prisoner.getJob() == Job.MINER) {
                if (type.equals("soon")) {
                    player.sendMessage(colorize(getConfig().getString("messages.mineReloadSoon")).replace("%name%", mineName));
                } else if (type.equals("reload")) {
                    player.sendMessage(colorize(getConfig().getString("messages.mineReload")).replace("%name%", mineName));
                }
            }
        }
    }

    private boolean isInside(Player player, Location minimum, Location maximum) {
        Location location = player.getLocation();
        World world = location.getWorld();

        int minX = minimum.getBlockX();
        int minY = minimum.getBlockY();
        int minZ = minimum.getBlockZ();

        int maxX = maximum.getBlockX();
        int maxY = maximum.getBlockY();
        int maxZ = maximum.getBlockZ();

        return location.getWorld().equals(world)
                && (location.getX() >= minX && location.getX() <= maxX)
                && (location.getY() >= minY && location.getY() <= maxY)
                && (location.getZ() >= minZ && location.getZ() <= maxZ);
    }

    private void mineReset(String name) {
        World world = Bukkit.getWorld(getConfig().getString("miners." + name + ".world"));
        int x1 = getConfig().getInt("miners." + name + ".1.x");
        int y1 = getConfig().getInt("miners." + name + ".1.y");
        int z1 = getConfig().getInt("miners." + name + ".1.z");
        Location pos1 = new Location(world, x1, y1, z1);

        int x2 = getConfig().getInt("miners." + name + ".2.x");
        int y2 = getConfig().getInt("miners." + name + ".2.y");
        int z2 = getConfig().getInt("miners." + name + ".2.z");
        Location pos2 = new Location(world, x2, y2, z2);

        Selection selection = new CuboidSelection(world, pos1, pos2);

        List<String> blocksID = new ArrayList<>();
        Map<String, Integer> blocks = new HashMap<>();
        int blocksCount = selection.getHeight() * selection.getWidth() * selection.getLength();

        for (String id : getConfig().getConfigurationSection("miners." + name + ".blocks").getKeys(false)) {
            double x = getConfig().getInt("miners." + name + ".blocks." + id) / 100 * blocksCount;
            blocks.put(id, (int) Math.round(x));
            blocksID.add(id);
        }

        int minX = selection.getMinimumPoint().getBlockX();
        int minY = selection.getMinimumPoint().getBlockY();
        int minZ = selection.getMinimumPoint().getBlockZ();

        int maxX = selection.getMaximumPoint().getBlockX();
        int maxY = selection.getMaximumPoint().getBlockY();
        int maxZ = selection.getMaximumPoint().getBlockZ();

        for (Player player : getServer().getOnlinePlayers()) {
            if (isInside(player, selection.getMinimumPoint(), selection.getMaximumPoint())) {
                int x = getConfig().getInt("miners." + name + ".point.x");
                int y = getConfig().getInt("miners." + name + ".point.y");
                int z = getConfig().getInt("miners." + name + ".point.z");

                player.teleport(new Location(world, x, y, z));
            }
        }

        Random rand = new Random();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    String block = null;
                    while (true) {
                        boolean c = false;
                        int id = rand.nextInt(blocksID.size());
                        if (blocks.get(blocksID.get(id)) != 0) {
                            block = blocksID.get(id);
                            c = true;
                        }
                        if (c) {
                            break;
                        }
                    }
                    world.getBlockAt(x, y, z).setType(Material.valueOf(block));
                    blocks.replace(block, blocks.get(block) - 1);
                }
            }
        }
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

        section = getConfig().getConfigurationSection("miner.blocks");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                minerBlockValues.put(id, getConfig().getInt("miner.blocks." + id));
            }
        }

        section = getConfig().getConfigurationSection("orePoints");
        if (section != null) {
            for (String name : section.getKeys(false)) {
                World world = Bukkit.getWorld(getConfig().getString("orePoints." + name + ".world"));
                int x = getConfig().getInt("orePoints." + name + ".x");
                int y = getConfig().getInt("orePoints." + name + ".y");
                int z = getConfig().getInt("orePoints." + name + ".z");

                orePoints.add(new Location(world, x, y, z));
            }
        }

        section = getConfig().getConfigurationSection("oreStorages");
        if (section != null) {
            for (String name : section.getKeys(false)) {
                World world = Bukkit.getWorld(getConfig().getString("oreStorages." + name + ".world"));
                int x = getConfig().getInt("oreStorages." + name + ".x");
                int y = getConfig().getInt("oreStorages." + name + ".y");
                int z = getConfig().getInt("oreStorages." + name + ".z");

                oreStorages.add(new Location(world, x, y, z));
            }
        }

        section = getConfig().getConfigurationSection("ironStorages");
        if (section != null) {
            for (String name : section.getKeys(false)) {
                World world = Bukkit.getWorld(getConfig().getString("ironStorages." + name + ".world"));
                int x = getConfig().getInt("ironStorages." + name + ".x");
                int y = getConfig().getInt("ironStorages." + name + ".y");
                int z = getConfig().getInt("ironStorages." + name + ".z");

                ironStorages.add(new Location(world, x, y, z));
            }
        }

        saveConfig();
    }


}
