package ru.prisonlife.pljobs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.plugin.PLPlugin;
import ru.prisonlife.util.InventoryUtil;

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

        if (!prisoner.hasOverdueJobSalary() || prisoner.getOverdueJobSalary() == 0) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.prisonerHasNotOverdueSalary")));
            return true;
        }

        if (prisoner.getJob() != Job.NONE) {
            player.sendMessage(colorize("&l&cВы сможете получить просроченную зарплату после окончания работы!"));
            return true;
        }

        if (!PrisonLife.getCurrencyManager().canPuttedMoney(player.getInventory(), prisoner.getOverdueJobSalary())) {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.notEnoughSlots")));
            return true;
        }

        InventoryUtil.putItemStacks(player.getInventory(), PrisonLife.getCurrencyManager().createMoney(prisoner.getOverdueJobSalary()));

        player.sendMessage(colorize(plugin.getConfig().getString("messages.getOverdueSalary")));
        return true;
    }
}
