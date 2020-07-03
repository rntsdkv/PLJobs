package ru.prisonlife.pljobs.commands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.prisonlife.plugin.PLPlugin;

import java.util.HashMap;
import java.util.Map;

import static ru.prisonlife.pljobs.Main.colorize;

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

        String world = selection.getWorld().getName();

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

        config.set("miners." + name + ".point.x", 0);
        config.set("miners." + name + ".point.y", 0);
        config.set("miners." + name + ".point.z", 0);

        player.sendMessage(colorize("messages.newMiner").replace("%name%", name));
        plugin.saveConfig();
        return true;
    }

    private WorldEditPlugin getWorldEdit() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (plugin instanceof WorldEditPlugin) return (WorldEditPlugin) plugin;
        return null;
    }
}
