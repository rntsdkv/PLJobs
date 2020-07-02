package ru.prisonlife.pljobs.events;


import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.colorize;
import static ru.prisonlife.pljobs.commands.SetGarbage.garbagePlayers;

public class ChestClick implements Listener {

    private PLPlugin plugin;

    public ChestClick(PLPlugin main) {
        this.plugin = main;
    }

    @EventHandler
    public void onChestClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block.getType() == Material.CHEST && garbagePlayers.contains(player)) {
                String path = "chests." + block.getX() + "&" + block.getY() + "&" + block.getZ();
                if (plugin.getConfig().getConfigurationSection(path) == null) {
                    plugin.getConfig().set(path + ".world", block.getWorld().getName());
                    plugin.getConfig().set(path + ".x", block.getX());
                    plugin.getConfig().set(path + ".y", block.getX());
                    plugin.getConfig().set(path + ".z", block.getX());
                    player.sendMessage(colorize("&l&6Вы установили мусорный бак!"));
                    garbagePlayers.remove(player);
                }
            }
        }
    }
}
