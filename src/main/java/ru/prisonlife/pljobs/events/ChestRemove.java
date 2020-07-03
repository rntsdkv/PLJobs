package ru.prisonlife.pljobs.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.colorize;

public class ChestRemove implements Listener {

    private PLPlugin plugin;
    public ChestRemove(PLPlugin main) {
        this.plugin = main;
    }

    @EventHandler
    public void onChestRemove(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (block.getType() == Material.CHEST) {
            FileConfiguration config = plugin.getConfig();
            if (config.getConfigurationSection("chest." + block.getLocation().toString()) != null) {
                config.set("chest." + block.getLocation().toString(), null);
                player.sendMessage(colorize("&l&6Вы убрали мусорный бак!"));
                plugin.saveConfig();
            }
        }
    }
}
