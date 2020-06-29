package ru.prisonlife.pljobs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.prisonlife.plugin.PLPlugin;

import java.util.ArrayList;
import java.util.List;

import static ru.prisonlife.pljobs.Main.colorize;

public class SetGarbage implements CommandExecutor {

    private PLPlugin plugin;
    public static List<Player> garbagePlayers = new ArrayList<>();

    public SetGarbage(PLPlugin main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(plugin.getConfig().getString("messages.wrongSender"));
            return true;
        }

        Player player = (Player) commandSender;

        if (!garbagePlayers.contains(player)) {
            garbagePlayers.add(player);
        }

        player.sendMessage(colorize("&l&6Теперь тыкните по сундуку и... о магия... у вас новый мусорный бак! Поздравляем!"));
        return true;
    }
}
