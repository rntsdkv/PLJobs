package ru.prisonlife.pljobs.events;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.prisonlife.pljobs.GarbageChest;
import ru.prisonlife.plugin.PLPlugin;

import java.util.Optional;

import static ru.prisonlife.pljobs.Main.colorize;
import static ru.prisonlife.pljobs.Main.garbageChests;
import static ru.prisonlife.pljobs.commands.SetGarbage.garbagePlayers;

public class ChestCleanerListener implements Listener {

    private final PLPlugin plugin;

    public ChestCleanerListener(PLPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChestClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();

        String world = block.getWorld().getName();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        GarbageChest garbage = new GarbageChest(world, x, y, z);

        if (block.getType() != Material.CHEST || !garbagePlayers.contains(player)) return;
        else if (garbage.exists()) return;

        garbageChests.add(garbage);
        player.sendMessage(colorize("&l&6Вы установили мусорный бак!"));
        garbagePlayers.remove(player);
    }

    @EventHandler
    public void onChestRemove(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        String world = block.getWorld().getName();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        GarbageChest garbage = new GarbageChest(world, x, y, z);

        if (block.getType() != Material.CHEST) return;
        else if (!garbage.exists()) return;

        player.sendMessage(colorize("&l&6Вы убрали мусорный бак!"));
    }

}
