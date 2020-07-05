package ru.prisonlife.pljobs.events;


import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.currency.CurrencyManager;
import ru.prisonlife.plugin.PLPlugin;

import java.util.Optional;

import static ru.prisonlife.pljobs.Main.*;

public class GarbageListener implements Listener {

    private final PLPlugin plugin;
    public GarbageListener(PLPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGarbagePickup(EntityPickupItemEvent event) {
        Player player = (Player) event.getEntity();
        Prisoner prisoner = PrisonLife.getPrisoner(player);
        ItemStack item = event.getItem().getItemStack();

        if (item.getType() == Material.COCOA_BEANS) {
            if (prisoner.getJob() != Job.CLEANER) {
                event.setCancelled(true);
                return;
            }

            int price = plugin.getConfig().getInt("cleaner.garbagePickup");
            playersSalary.replace(player, playersSalary.get(player) + price);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + String.format("+%d$", price)));

            garbageCount -= item.getAmount();
        }

        if (item.getType() == Material.PLAYER_HEAD) {
            player.getInventory().remove(new ItemStack(Material.PLAYER_HEAD));
        }
    }

    @EventHandler
    public void onGarbageRemove(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Prisoner prisoner = PrisonLife.getPrisoner(player);
        Block block = event.getBlock();
        CurrencyManager currencyManager = PrisonLife.getCurrencyManager();
        Integer amount = plugin.getConfig().getInt("cleaner.garbageBreak");

        if (block.getType() != Material.PLAYER_HEAD) return;
        else if (prisoner.getJob() == Job.CLEANER) {
            event.setCancelled(true);
            player.sendMessage(colorize("&l&cТебе не хватает мусора?!"));
            return;
        }
        else if (!currencyManager.canPuttedMoney(player.getInventory(), amount)) {
            event.setCancelled(true);
            player.sendMessage(colorize("&l&6У вас нет места для денег!"));
            return;
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + String.format("+%d$", amount)));
        currencyManager.createMoney(amount).forEach(player.getInventory()::addItem);
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
                    int price = plugin.getConfig().getInt("cleaner.garbageAway");sss
                    playersSalary.replace(player, playersSalary.get(player) + price * amount);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + String.format("+%d$", price * amount)));
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    event.setCancelled(true);
                }

            }
        }
    }
}
