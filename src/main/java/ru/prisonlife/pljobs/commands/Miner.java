package ru.prisonlife.pljobs.commands;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.colorize;

/**
 * @author rntsdkv
 * @project PLJobs
 */

public class Miner implements CommandExecutor  {

    private PLPlugin plugin;
    public Miner(PLPlugin main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration config = plugin.getConfig();
        if (strings[0].equals("create")) {
            minerCreate(commandSender, strings, config);
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

        Selection selection = getWorldEdit().getSelection(player);

        if (selection == null) {
            player.sendMessage(colorize(config.getString("messages.selectionNotExists")));
            return true;
        }

        /*
        CONFIG:

        miners:
          name:
            world: WORLD
            min:
              x: X
              y: Y
              z: Z
            max:
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
         */

        String world = selection.getWorld().getName();

        int minX = selection.getMinimumPoint().getBlockX();
        int minY = selection.getMinimumPoint().getBlockY();
        int minZ = selection.getMinimumPoint().getBlockZ();

        int maxX = selection.getMaximumPoint().getBlockX();
        int maxY = selection.getMaximumPoint().getBlockY();
        int maxZ = selection.getMaximumPoint().getBlockZ();

        config.set("miners." + name + ".world", world);

        config.set("miners." + name + ".min.x", minX);
        config.set("miners." + name + ".min.y", minY);
        config.set("miners." + name + ".min.z", minZ);

        config.set("miners." + name + ".max.x", maxX);
        config.set("miners." + name + ".max.y", maxY);
        config.set("miners." + name + ".max.z", maxZ);

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
