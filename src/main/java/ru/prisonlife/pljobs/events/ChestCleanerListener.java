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
import ru.prisonlife.plugin.PLPlugin;

import java.util.Optional;

import static ru.prisonlife.pljobs.Main.colorize;
import static ru.prisonlife.pljobs.commands.SetGarbage.garbagePlayers;

public class ChestCleanerListener implements Listener {

    private final PLPlugin plugin;

    public ChestCleanerListener(PLPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChestClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        String path = String.format("chest.%d&%d&%d", block.getX(), block.getY(), block.getZ());

        if (block.getType() != Material.CHEST || !garbagePlayers.contains(player)) return;
        else if (Optional.ofNullable(config.getConfigurationSection(path)).isPresent()) return;

        config.set(path + ".world", block.getWorld().getName());
        config.set(path + ".x", block.getX());
        config.set(path + ".y", block.getX());
        config.set(path + ".z", block.getX());
        player.sendMessage(colorize("&l&6Вы установили мусорный бак!"));
        garbagePlayers.remove(player);

        plugin.saveConfig();
    }

    @EventHandler
    public void onChestRemove(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        if (block.getType() != Material.CHEST) return;
        else if (!hasChestDataInConfig(config, block)) return;

        config.set("chest." + block.getLocation().toString(), null);
        player.sendMessage(colorize("&l&6Вы убрали мусорный бак!"));
        plugin.saveConfig();
    }

    private boolean hasChestDataInConfig(FileConfiguration configuration, Block block) {
        return Optional.ofNullable(configuration.getConfigurationSection("chest." + block.getLocation())).isPresent();
    }
}
