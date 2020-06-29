package ru.prisonlife.pljobs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.colorize;

public class GetSalary implements CommandExecutor {

    private PLPlugin plugin;
    public GetSalary(PLPlugin main) {
        this.plugin = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(plugin.getConfig().getString("messages.wrongSender"));
            return true;
        }

        Player player = (Player) commandSender;
        Prisoner prisoner = PrisonLife.getPrisoner(player);

        if (!prisoner.hasOverdueJobSalary()) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.prisonerHasNotOverdueSalary")));
            return true;
        }

        if (!PrisonLife.getCurrencyManager().canPuttedMoney(player.getInventory(), prisoner.getOverdueJobSalary())) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.notEnoughSlots")));
            return true;
        }

        player.getInventory().addItem((ItemStack) PrisonLife.getCurrencyManager().createMoney(prisoner.getOverdueJobSalary()));
        player.sendMessage(colorize(plugin.getConfig().getString("messages.getOverdueSalary")));
        return true;
    }
}
