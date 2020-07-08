package ru.prisonlife.pljobs.commands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.prisonlife.plugin.PLPlugin;

import java.util.*;

import static ru.prisonlife.pljobs.Main.*;

/**
 * @author rntsdkv
 * @project PLJobs
 */

public class Miner implements CommandExecutor  {

    public static Map<Player, Location> position1 = new HashMap<>();
    public static Map<Player, Location> position2 = new HashMap<>();

    private PLPlugin plugin;
    public Miner(PLPlugin main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration config = plugin.getConfig();
        if (strings.length > 0) {

            if (strings[0].equals("pos1") || strings[0].equals("pos2")) {
                minerPosition(commandSender, strings, config);
            } else if (strings[0].equals("create")) {
                minerCreate(commandSender, strings, config);
            } else if (strings[0].equals("timer")) {
                minerTimer(commandSender, strings, config);
            } else if (strings[0].equals("setpoint")) {
                minerSetPoint(commandSender, strings, config);
            } else if (strings[0].equals("delete")) {
                minerDelete(commandSender, strings, config);
            } else if (strings[0].equals("set")) {
                minerSet(commandSender, strings, config);
            } else if (strings[0].equals("on")) {
                minerOn(commandSender, strings, config);
            } else if (strings[0].equals("off")) {
                minerOff(commandSender, strings, config);
            } else if (strings[0].equals("orepoint")) {
                minerOrePoint(commandSender, strings, config);
            } else if (strings[0].equals("orestorage")) {
                minerOreStorage(commandSender, strings, config);
            } else if (strings[0].equals("ironstorage")) {
                minerIronStorage(commandSender, strings, config);
            }
        }
        return true;
    }

    private boolean minerPosition(CommandSender sender, String[] strings, FileConfiguration config) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(config.getString("messages.wrongSender"));
            return true;
        }

        Player player = (Player) sender;

        if (strings[0].equals("pos1")) {
            position1.put(player, player.getLocation());
            player.sendMessage(colorize(config.getString("messages.pos1Add")));
            return true;
        }

        if (strings[0].equals("pos2")) {
            position2.put(player, player.getLocation());
            player.sendMessage(colorize(config.getString("messages.pos2Add")));
            return true;
        }

        return true;
    }

    private boolean minerCreate(CommandSender sender, String[] strings, FileConfiguration config) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(config.getString("messages.wrongSender"));
            return true;
        }

        Player player = (Player) sender;

        if (strings.length != 2) {
            player.sendMessage(colorize(config.getString("messages.wrongCommandArguments")));
            return false;
        }

        String name = strings[1];

        if (config.getConfigurationSection("miners." + name) != null) {
            player.sendMessage(colorize("messages.minerIsAlreadyExists"));
            return true;
        }

        if (!position1.containsKey(player) || !position2.containsKey(player)) {
            player.sendMessage(colorize(config.getString("messages.selectionNotExists")));
            return true;
        }

        Location pos1 = position1.get(player);
        Location pos2 = position2.get(player);

        /*
        CONFIG:

        miners:
          name:
            world: WORLD
            1:
              x: X
              y: Y
              z: Z
            2:
              x: X
              y: Y
              z: Z
            blocks:
              id: ID
              pr: %
            time: TIME
            point:
              x: X
              y: Y
              z: Z

         Selection selection = new CuboidSelection(player.getWorld(), pos1, pos2);
         */

        String world = player.getWorld().getName();

        int x1 = pos1.getBlockX();
        int y1 = pos1.getBlockY();
        int z1 = pos1.getBlockZ();

        int x2 = pos2.getBlockX();
        int y2 = pos2.getBlockY();
        int z2 = pos2.getBlockZ();

        config.set("miners." + name + ".world", world);

        config.set("miners." + name + ".1.x", x1);
        config.set("miners." + name + ".1.y", y1);
        config.set("miners." + name + ".1.z", z1);

        config.set("miners." + name + ".2.x", x2);
        config.set("miners." + name + ".2.y", y2);
        config.set("miners." + name + ".2.z", z2);

        player.sendMessage(colorize("messages.newMiner").replace("%name%", name));
        plugin.saveConfig();
        return true;
    }

    private boolean minerTimer(CommandSender sender, String[] strings, FileConfiguration config) {
        if (strings.length != 3) {
            sender.sendMessage(colorize(config.getString("messages.wrongCommandArguments")));
            return false;
        }

        String name = strings[1];
        int time = Integer.parseInt(strings[2]);

        if (config.getConfigurationSection("miners." + name) == null) {
            sender.sendMessage(colorize(config.getString("messages.minerNotExists")));
            return true;
        }

        config.set("miners." + name + ".time", time);

        sender.sendMessage(colorize("messages.timeSet"));
        plugin.saveConfig();
        return true;
    }

    private boolean minerSetPoint(CommandSender sender, String[] strings, FileConfiguration config) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(config.getString("messages.wrongSender"));
            return true;
        }

        Player player = (Player) sender;

        if (strings.length != 2) {
            player.sendMessage(colorize(config.getString("messages.wrongCommandArguments")));
            return false;
        }

        String name = strings[1];

        if (config.getConfigurationSection("miners." + name) == null) {
            sender.sendMessage(colorize(config.getString("messages.minerNotExists")));
            return true;
        }

        Location location = player.getLocation();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        config.set("miners." + name + ".point.x", x);
        config.set("miners." + name + ".point.y", y);
        config.set("miners." + name + ".point.z", z);

        player.sendMessage(colorize(config.getString("messages.pointSet")));
        plugin.saveConfig();
        return true;
    }

    private boolean minerDelete(CommandSender sender, String[] strings, FileConfiguration config) {
        if (strings.length != 2) {
            sender.sendMessage(colorize(config.getString("messages.wrongCommandArguments")));
            return false;
        }

        String name = strings[1];

        if (config.getConfigurationSection("miners." + name) == null) {
            sender.sendMessage(colorize(config.getString("messages.minerNotExists")));
            return true;
        }

        config.set("miners." + name, null);
        sender.sendMessage(colorize(config.getString("messages.minerDeleted")));

        if (minerTime.containsKey(name)) {
            minerTime.remove(name);
        }

        plugin.saveConfig();
        return true;
    }

    private boolean minerSet(CommandSender sender, String[] strings, FileConfiguration config) {
        if (strings.length < 3) {
            sender.sendMessage(colorize(config.getString("messages.wrongCommandArguments")));
            return true;
        }

        String name = strings[1];

        if (config.getConfigurationSection("miners." + name) == null) {
            sender.sendMessage(colorize(config.getString("messages.minerNotExists")));
            return true;
        }

        int pr_sum = 0;

        for (int i = 2; i < strings.length; i++) {
            int pr = Integer.parseInt(strings[i].split(":")[1]);
            pr_sum += pr;
        }

        if (pr_sum != 100) {
            sender.sendMessage(colorize(config.getString("messages.prSumMustBe100")));
            return true;
        }

        if (config.getConfigurationSection("miners." + name + ".blocks") != null) {
            config.set("miners." + name + ".blocks", null);
        }

        for (int i = 2; i < strings.length; i++) {
            String[] id_pr = strings[i].split(":");
            String id = id_pr[0];
            int pr = Integer.parseInt(id_pr[1]);
            config.set("miners." + name + ".blocks." + id, pr);
        }

        sender.sendMessage(colorize(config.getString("messages.blocksSet")));
        plugin.saveConfig();
        return true;
    }
    
    private boolean minerOn(CommandSender sender, String[] strings, FileConfiguration config) {
        if (strings.length != 2) {
            sender.sendMessage(colorize(config.getString("messages.wrongCommandArguments")));
            return false;
        }
        
        String name = strings[1];
        
        if (config.getConfigurationSection("miners." + name) == null) {
            sender.sendMessage(colorize(config.getString("messages.minerNotExists")));
            return true;
        }

        String time = config.getString("miners." + name + ".time");
        ConfigurationSection blocks = config.getConfigurationSection("miners." + name + ".blocks");

        if (time != null && blocks != null) {
            if (!minerTime.containsKey(name)) {
                minerTime.put(name, config.getInt("miners." + name + ".time") - 1);
                sender.sendMessage("Шахта успешно включена!");
            } else {
                sender.sendMessage("Шахта итак работает!");
            }
        } else {
            sender.sendMessage("Невозможно включить шахту! Добавьте время и блоки.");
        }
        return true;
    }

    private boolean minerOff(CommandSender sender, String[] strings, FileConfiguration config) {
        if (strings.length != 2) {
            sender.sendMessage(colorize(config.getString("messages.wrongCommandArguments")));
            return false;
        }

        String name = strings[1];

        if (config.getConfigurationSection("miners." + name) == null) {
            sender.sendMessage(colorize(config.getString("messages.minerNotExists")));
            return true;
        }

        if (!minerTime.containsKey(name)) {
            sender.sendMessage("Шахта итак не работает!");
        } else {
            minerTime.remove(name);
            sender.sendMessage("Шахта успешно выключена!");
        }
        return true;
    }
    
    private boolean minerOrePoint(CommandSender sender, String[] strings, FileConfiguration config) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(config.getString("messages.wrongSender"));
            return true;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        orePoint = location;

        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        config.set("orePoint.world", world.getName());
        config.set("orePoint.x", x);
        config.set("orePoint.y", y);
        config.set("orePoint.z", z);

        player.sendMessage("Точка OrePoint установлена!");
        plugin.saveConfig();
        return true;
    }

    private boolean minerOreStorage(CommandSender sender, String[] strings, FileConfiguration config) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(config.getString("messages.wrongSender"));
            return true;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        oreStorage = location;

        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        config.set("oreStorage.world", world.getName());
        config.set("oreStorage.x", x);
        config.set("oreStorage.y", y);
        config.set("oreStorage.z", z);

        player.sendMessage("Точка OreStorage установлена!");
        plugin.saveConfig();
        return true;
    }

    private boolean minerIronStorage(CommandSender sender, String[] strings, FileConfiguration config) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(config.getString("messages.wrongSender"));
            return true;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        ironStorage = location;

        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        config.set("ironStorage.world", world.getName());
        config.set("ironStorage.x", x);
        config.set("ironStorage.y", y);
        config.set("ironStorage.z", z);

        player.sendMessage("Точка IronStorage установлена!");
        plugin.saveConfig();
        return true;
    }
}
