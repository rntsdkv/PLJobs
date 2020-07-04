package ru.prisonlife.pljobs.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.database.json.ItemSlot;
import ru.prisonlife.plugin.PLPlugin;
import ru.prisonlife.util.InventoryUtil;

import java.util.List;

import static ru.prisonlife.pljobs.Main.*;

/**
 * @author rntsdkv
 * @project PLJobs
 */

public class WorkerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Prisoner prisoner = PrisonLife.getPrisoner(player);

        List<ItemSlot> items = PrisonLife.getStoredInventory(prisoner.getAccountNumber());
        InventoryUtil.putItemSlots(player.getInventory(), items);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Prisoner prisoner = PrisonLife.getPrisoner(player);

        if (prisoner.getJob() != Job.NONE) {
            if (prisoner.hasOverdueJobSalary()) {
                prisoner.setOverdueJobSalary(prisoner.getOverdueJobSalary() + playersSalary.get(player));
            } else {
                if (playersSalary.get(player) != 0) {
                    prisoner.setOverdueJobSalary(playersSalary.get(player));
                }
            }
            playersSalary.remove(player);
            prisoner.setJob(Job.NONE);
            player.getInventory().clear();

            if (getWorkerCount("cleaner") == 0) {
                taskGarbages.cancel();
            }
        }
    }
}
