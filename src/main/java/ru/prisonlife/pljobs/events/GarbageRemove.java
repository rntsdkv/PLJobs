package ru.prisonlife.pljobs.events;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.colorize;

public class GarbageRemove implements Listener {

    private PLPlugin plugin;
    public GarbageRemove(PLPlugin main) {
        this.plugin = main;
    }

    @EventHandler
    public void onGarbageRemove(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Prisoner prisoner = PrisonLife.getPrisoner(player);
        Block block = event.getBlock();

        if (block.getType() == Material.PLAYER_HEAD) {
            if (prisoner.getJob() == Job.NONE) {
                event.setCancelled(true);
            } else {
                Integer amount = plugin.getConfig().getInt("cleaner.garbageBreak");
                if (PrisonLife.getCurrencyManager().canPuttedMoney(player.getInventory(), amount)) {
                    player.getInventory().addItem((ItemStack) PrisonLife.getCurrencyManager().createMoney(amount));
                } else {
                    player.sendMessage(colorize("&l&6У вас нет места для денег!"));
                    event.setCancelled(true);
                }
            }
        }
    }
}
