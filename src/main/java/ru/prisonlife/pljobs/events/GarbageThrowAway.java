package ru.prisonlife.pljobs.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.playersSalary;

public class GarbageThrowAway implements Listener {

    private PLPlugin plugin;
    public GarbageThrowAway(PLPlugin main) {
        this.plugin = main;
    }

    @EventHandler
    public void onGarbageThrowAway(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Prisoner prisoner = PrisonLife.getPrisoner(player);
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
            if (block.getType() == Material.CHEST && prisoner.getJob() == Job.CLEANER && player.getInventory().getItemInMainHand().getType() == Material.COCOA_BEANS) {

                if (plugin.getConfig().getConfigurationSection("chest." + block.getLocation().toString()) != null) {
                    int amount = player.getInventory().getItemInMainHand().getAmount();
                    playersSalary.replace(player, playersSalary.get(player) + plugin.getConfig().getInt("cleaner.garbageAway") * amount);
                    player.getInventory().setItemInMainHand(null);
                }

            }
        }
    }
}
