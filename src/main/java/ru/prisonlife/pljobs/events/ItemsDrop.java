package ru.prisonlife.pljobs.events;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;

import static ru.prisonlife.pljobs.Main.minerBlockValues;

public class ItemsDrop implements Listener {

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        Prisoner prisoner = PrisonLife.getPrisoner(event.getPlayer());
        if ((item.getType() == Material.IRON_SHOVEL || item.getType() == Material.COCOA_BEANS) && prisoner.getJob() == Job.CLEANER) {
            event.setCancelled(true);
        }
        if (prisoner.getJob() == Job.MINER) {
            if (minerBlockValues.containsKey(item.getType().name()) || item.getType() == Material.IRON_PICKAXE) event.setCancelled(true);
            if (item.getType() == Material.IRON_ORE || item.getType() == Material.IRON_INGOT) event.setCancelled(true);
        }
    }
}
