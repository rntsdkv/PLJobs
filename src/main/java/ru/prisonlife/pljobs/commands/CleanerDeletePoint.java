package ru.prisonlife.pljobs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.cleanerPoints;
import static ru.prisonlife.pljobs.Main.colorize;

public class CleanerDeletePoint implements CommandExecutor {

    private PLPlugin plugin;
    public CleanerDeletePoint(PLPlugin main) {
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

        if (strings.length != 1) {
            player.sendMessage(colorize(config.getString("messages.wrongCommandArguments")));
            return false;
        }

        if (config.getConfigurationSection("cleaners." + strings[0]) == null) {
            player.sendMessage(colorize(config.getString("messages.pointNotExists")));
            return true;
        }

        config.set("cleaners." + strings[0], null);
        cleanerPoints.remove(Integer.parseInt(strings[0]));

        player.sendMessage(colorize("&l&6Точка №" + strings[0] + " удалена!"));
        return true;
    }
}
