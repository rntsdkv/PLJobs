package ru.prisonlife.pljobs;

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

    public static List<GarbageChest> garbageChests = new ArrayList<>();
    public static Map<Integer, Location> cleanerPoints = new HashMap<>();
    public static Integer garbageCount = 0;
    public static BukkitTask taskGarbages;

    public static Map<String, Integer> minerTime = new HashMap<>();
    public static Map<String, Integer> minerBlockValues = new HashMap<>();
    public static BukkitTask taskMine;

    public static Location orePoint;
    public static OreStorage oreStorage;
    public static Location ironStorage;
    public static List<Location> minerFurnaces = new ArrayList<>();

    public static int ironCount = 0;
    public static int ironMax = 0;

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
            for (GarbageChest garbage : garbageChests) {
                garbage.loadToConfig();
            }
        }

        if (oreStorage != null) {
            getConfig().set("oreCount", oreStorage.getCount());
            getConfig().set("oreMax", oreStorage.getMaximum());

            Location location = oreStorage.getLocation();

            String world = location.getWorld().getName();
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();

            getConfig().set("oreStorage.world", world);
            getConfig().set("oreStorage.x", x);
            getConfig().set("oreStorage.y", y);
            getConfig().set("oreStorage.z", z);
        }

        getConfig().set("ironCount", ironCount);

        if (minerFurnaces.size() != 0) {
            for (Location furnace : minerFurnaces) {
                int x = furnace.getBlockX();
                int y = furnace.getBlockY();
                int z = furnace.getBlockZ();

                String name = String.format("%d&%d&%d", x, y, z);
                getConfig().set("furnaces." + name + ".world", furnace.getWorld().getName());
                getConfig().set("furnaces." + name + ".x", x);
                getConfig().set("furnaces." + name + ".y", y);
                getConfig().set("furnaces." + name + ".y", y);
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
        ConfigurationSection section = getConfig().getConfigurationSection("cleaners");
        if (section != null) {
            for (String id : section.getKeys(false)) {

                String world = getConfig().getString("cleaners." + id + ".world");
                int x = getConfig().getInt("cleaners." + id + ".x");
                int y = getConfig().getInt("cleaners." + id + ".y");
                int z = getConfig().getInt("cleaners." + id + ".z");

                cleanerPoints.put(Integer.parseInt(id), new Location(Bukkit.getWorld(world), x, y, z));
            }
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

    private boolean isInside(Player player, Location pos1, Location pos2) {
        Location location = player.getLocation();
        World world = location.getWorld();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());

        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

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

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);

        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        List<String> blocksID = new ArrayList<>();
        Map<String, Integer> blocks = new HashMap<>();
        int blocksCount = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocksCount ++;
                }
            }
        }
        for (String id : getConfig().getConfigurationSection("miners." + name + ".blocks").getKeys(false)) {
            float pr = (float) getConfig().getInt("miners." + name + ".blocks." + id) / 100;
            double x = (double) pr * blocksCount;
            blocks.put(id, (int) Math.round(x));
            blocksID.add(id);
        }

        int pointX = getConfig().getInt("miners." + name + ".point.x");
        int pointY = getConfig().getInt("miners." + name + ".point.y");
        int pointZ = getConfig().getInt("miners." + name + ".point.z");
        Location point = new Location(world, pointX, pointY, pointZ);
        for (Player player : getServer().getOnlinePlayers()) {
            if (isInside(player, pos1, pos2)) {
                player.teleport(point);
            }
        }
        Random rand = new Random();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    while (true) {
                        int id = rand.nextInt(blocksID.size());
                        if (blocks.get(blocksID.get(id)) != 0) {
                            String block = blocksID.get(id);
                            world.getBlockAt(x, y, z).setType(Material.valueOf(block));
                            blocks.replace(block, blocks.get(block) - 1);
                            break;
                        }
                    }
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
                String world = getConfig().getString("garbageChests." + id + ".world");
                int x = getConfig().getInt("garbageChests." + id + ".x");
                int y = getConfig().getInt("garbageChests." + id + ".y");
                int z = getConfig().getInt("garbageChests." + id + ".z");
                GarbageChest garbage = new GarbageChest(world, x, y, z);
                garbageChests.add(garbage);
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

        section = getConfig().getConfigurationSection("orePoint");
        if (section != null) {
            World world = Bukkit.getWorld(getConfig().getString("orePoint.world"));
            int x = getConfig().getInt("orePoint.x");
            int y = getConfig().getInt("orePoint.y");
            int z = getConfig().getInt("orePoint.z");

            orePoint = new Location(world, x, y, z);
        }

        section = getConfig().getConfigurationSection("oreStorage");
        if (section != null) {
            World world = Bukkit.getWorld(getConfig().getString("oreStorage.world"));
            int x = getConfig().getInt("oreStorage.x");
            int y = getConfig().getInt("oreStorage.y");
            int z = getConfig().getInt("oreStorage.z");
            Location location = new Location(world, x, y, z);

            int count = getConfig().getInt("oreCount");
            int max = getConfig().getInt("oreMax");
            oreStorage = new OreStorage(location, count, max);
        }

        section = getConfig().getConfigurationSection("ironStorage");
        if (section != null) {
            World world = Bukkit.getWorld(getConfig().getString("ironStorage.world"));
            int x = getConfig().getInt("ironStorage.x");
            int y = getConfig().getInt("ironStorage.y");
            int z = getConfig().getInt("ironStorage.z");

            ironStorage = new Location(world, x, y, z);
        }

        if (getConfig().getString("ironCount") != null) {
            ironCount = getConfig().getInt("ironCount");
        }
        if (getConfig().getString("ironMax") != null) {
            ironMax = getConfig().getInt("ironMax");
        }

        section = getConfig().getConfigurationSection("furnaces");
        if (section != null) {
            for (String name : section.getKeys(false)) {
                World world = Bukkit.getWorld(getConfig().getString("furnaces." + name + ".world"));
                int x = getConfig().getInt("furnaces." + name + ".x");
                int y = getConfig().getInt("furnaces." + name + ".y");
                int z = getConfig().getInt("furnaces." + name + ".z");

                minerFurnaces.add(new Location(world, x, y, z));
            }
            getConfig().set("furnaces", null);
        }

        saveConfig();
    }


}
