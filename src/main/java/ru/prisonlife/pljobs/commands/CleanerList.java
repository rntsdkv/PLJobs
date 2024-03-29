package ru.prisonlife.pljobs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ru.prisonlife.plugin.PLPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.prisonlife.pljobs.Main.colorize;

public class CleanerList implements CommandExecutor {

    private PLPlugin plugin;
    public CleanerList(PLPlugin main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("cleaners");

        if (commandSender instanceof Player) {
            if (!Optional.ofNullable(section).isPresent()) {
                commandSender.sendMessage(colorize("&l&6Точек спавна мусора еще не создано!"));
            } else {
                commandSender.sendMessage(colorize("&l&6Список точек спавна мусора:\n" + String.join(", ", getGarbages(section))));
            }
        } else {
            if (!Optional.ofNullable(section).isPresent()) {
                commandSender.sendMessage("Точек спавна мусора еще не создано!");
            } else {
                commandSender.sendMessage("Список точек спавна мусора:\n" + String.join(", ", getGarbages(section)));
            }
        }
        return true;
    }

    private List<String> getGarbages(ConfigurationSection section) {
        List<String> garbages = new ArrayList<>();

        for (String id : section.getKeys(false)) {

            String x = plugin.getConfig().getString("cleaners." + id + ".x");
            String y = plugin.getConfig().getString("cleaners." + id + ".y");
            String z = plugin.getConfig().getString("cleaners." + id + ".z");

            garbages.add(id + "(" + String.join(", ", x, y, z) + ")");
        }

        return garbages;
    }
}
