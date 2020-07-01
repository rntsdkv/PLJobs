package ru.prisonlife.pljobs.events;


import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.playersSalary;
import static ru.prisonlife.pljobs.Main.garbageCount;

public class GarbagePickup implements Listener {

    private PLPlugin plugin;
    public GarbagePickup(PLPlugin main) {
        this.plugin = main;
    }

    @EventHandler
    public void onGarbagePickup(EntityPickupItemEvent event) {
        Player player = (Player) event.getEntity();
        Prisoner prisoner = PrisonLife.getPrisoner(player);
        Item item = event.getItem();

        if (item.getItemStack().getType() == Material.COCOA_BEANS) {
            if (prisoner.getJob() == Job.CLEANER) {
                if (playersSalary.containsKey(player)) {
                    playersSalary.replace(player, playersSalary.get(player) + plugin.getConfig().getInt("cleaner.garbagePickup"));
                } else {
                    playersSalary.put(player, plugin.getConfig().getInt("cleaner.garbagePickup"));
                }

                garbageCount --;
            } else {
                event.setCancelled(true);
            }
        }
    }
}
