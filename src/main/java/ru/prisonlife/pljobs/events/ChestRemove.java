package ru.prisonlife.pljobs.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import ru.prisonlife.plugin.PLPlugin;

import java.util.Optional;

import static ru.prisonlife.pljobs.Main.colorize;

public class ChestRemove implements Listener {

    private final PLPlugin plugin;
    public ChestRemove(PLPlugin plugin) {
        this.plugin = plugin;
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
