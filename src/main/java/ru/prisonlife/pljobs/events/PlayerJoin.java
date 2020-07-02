package ru.prisonlife.pljobs.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.database.json.ItemSlot;
import ru.prisonlife.plugin.PLPlugin;
import ru.prisonlife.util.InventoryUtil;

import java.util.List;

/**
 * @author rntsdkv
 * @project PLJobs
 */

public class PlayerJoin implements Listener {

    private PLPlugin plugin;
    public PlayerJoin(PLPlugin main) {
        this.plugin = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Prisoner prisoner = PrisonLife.getPrisoner(player);

        List<ItemSlot> items = PrisonLife.getStoredInventory(prisoner.getAccountNumber());
        InventoryUtil.putItemSlots(player.getInventory(), items);
    }
}
