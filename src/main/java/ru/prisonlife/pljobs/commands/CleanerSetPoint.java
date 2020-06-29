package ru.prisonlife.pljobs.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.cleanerPoints;
import static ru.prisonlife.pljobs.Main.colorize;

public class CleanerSetPoint implements CommandExecutor {

    private PLPlugin plugin;
    public CleanerSetPoint(PLPlugin main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration config = plugin.getConfig();

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(config.getString("messages.wrongSender"));
            return true;
        }

        Player player = (Player) commandSender;

        config.set("cleanerPointCount", config.getInt("cleanerPointCount") + 1);

        String world = player.getWorld().getName();
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        config.set("cleaners." + config.getString("cleanerPointCount") + ".world", world);
        config.set("cleaners." + config.getString("cleanerPointCount") + ".x", x);
        config.set("cleaners." + config.getString("cleanerPointCount") + ".y", y);
        config.set("cleaners." + config.getString("cleanerPointCount") + ".z", z);

        cleanerPoints.put(config.getInt("cleanerPointCount"), new Location(Bukkit.getWorld(world), x, y, z));

        player.sendMessage(colorize("&l&6Установлен новый спавн мусора №" + config.getString("cleanerPointCount")));

        return true;
    }
}
