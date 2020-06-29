package ru.prisonlife.pljobs.events;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.Plugin;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.plugin.PLPlugin;

public class ItemsDrop implements Listener {

    private PLPlugin plugin;
    public ItemsDrop(PLPlugin main) {
        this.plugin = main;
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        Prisoner prisoner = PrisonLife.getPrisoner(event.getPlayer());
        if ((item.getItemStack().getType() == Material.IRON_SHOVEL || item.getItemStack().getType() == Material.COCOA_BEANS) && prisoner.getJob() == Job.CLEANER) {
            event.setCancelled(true);
        }
    }
}
