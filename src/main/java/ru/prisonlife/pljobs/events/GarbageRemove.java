package ru.prisonlife.pljobs.events;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.currency.CurrencyManager;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.colorize;

public class GarbageRemove implements Listener {

    private final PLPlugin plugin;
    public GarbageRemove(PLPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGarbageRemove(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Prisoner prisoner = PrisonLife.getPrisoner(player);
        Block block = event.getBlock();
        CurrencyManager currencyManager = PrisonLife.getCurrencyManager();
        Integer amount = plugin.getConfig().getInt("cleaner.garbageBreak");

        if (block.getType() != Material.PLAYER_HEAD) return;
        else if (prisoner.getJob() != Job.CLEANER) {
            event.setCancelled(true);
            player.sendMessage(colorize("&l&cТебе не хватает мусора?!"));
            return;
        }
        else if (!currencyManager.canPuttedMoney(player.getInventory(), amount)) {
            event.setCancelled(true);
            player.sendMessage(colorize("&l&6У вас нет места для денег!"));
            return;
        }

        currencyManager.createMoney(amount).forEach(player.getInventory()::addItem);
    }
}
