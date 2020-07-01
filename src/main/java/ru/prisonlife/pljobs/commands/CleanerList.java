package ru.prisonlife.pljobs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.prisonlife.plugin.PLPlugin;
import java.util.ArrayList;
import java.util.List;
import static ru.prisonlife.pljobs.Main.colorize;

public class CleanerList implements CommandExecutor {

    private PLPlugin plugin;
    public CleanerList(PLPlugin main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            if (plugin.getConfig().getList("cleaners").size() == 0) {
                commandSender.sendMessage(colorize("&l&6Точек спавна мусора еще не создано!"));
            } else {
                commandSender.sendMessage(colorize("&l&6Список точек спавна мусора:\n" + String.join(", ", getGarbages())));
            }
        } else {
            if (plugin.getConfig().getList("cleaners").size() == 0) {
                commandSender.sendMessage("Точек спавна мусора еще не создано!");
            } else {
                commandSender.sendMessage("Список точек спавна мусора:\n" + String.join(", ", getGarbages()));
            }
        }
        return true;
    }

    private List<String> getGarbages() {
        List<String> garbages = new ArrayList<>();

        for (String id : plugin.getConfig().getConfigurationSection("cleaners").getKeys(false)) {

            String x = plugin.getConfig().getString("cleaners." + id + ".x");
            String y = plugin.getConfig().getString("cleaners." + id + ".y");
            String z = plugin.getConfig().getString("cleaners." + id + ".z");

            garbages.add(id + "(" + String.join(", ", x, y, z) + ")");
        }

        return garbages;
    }
}
