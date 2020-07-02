package ru.prisonlife.pljobs.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.*;

public class PlayerLeave implements Listener {

    private PLPlugin plugin;

    public PlayerLeave(PLPlugin main) {
        this.plugin = main;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
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

            if (getCleanersCount() == 0) {
                task.cancel();
            }
        }
    }
}
