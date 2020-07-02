package ru.prisonlife.pljobs.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
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

        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {

            Block block = event.getClickedBlock();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (block.getType() == Material.CHEST && prisoner.getJob() == Job.CLEANER && itemInHand.getType() == Material.COCOA_BEANS) {

                if (plugin.getConfig().getConfigurationSection("chests." + block.getX() + "&" + block.getY() + "&" + block.getZ()) != null) {
                    int amount = itemInHand.getAmount();
                    playersSalary.replace(player, playersSalary.get(player) + plugin.getConfig().getInt("cleaner.garbageAway") * amount);
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    event.setCancelled(true);
                }

            }
        }
    }
}
