package ru.prisonlife.pljobs.events;


import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
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
        ItemStack item = event.getItem().getItemStack();

        if (item.getType() == Material.COCOA_BEANS) {
            if (prisoner.getJob() == Job.CLEANER) {
                if (playersSalary.containsKey(player)) {
                    playersSalary.replace(player, playersSalary.get(player) + plugin.getConfig().getInt("cleaner.garbagePickup"));
                } else {
                    playersSalary.put(player, plugin.getConfig().getInt("cleaner.garbagePickup"));
                }

                garbageCount -= item.getAmount();
            } else {
                event.setCancelled(true);
            }
        }

        if (item.getType() == Material.PLAYER_HEAD) {
            if (prisoner.getJob() != Job.CLEANER) {
                player.getInventory().remove(new ItemStack(Material.PLAYER_HEAD));
            } else {
                event.setCancelled(true);
            }
        }
    }
}
