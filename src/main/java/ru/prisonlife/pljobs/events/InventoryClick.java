package ru.prisonlife.pljobs.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.database.json.ItemSlot;
import ru.prisonlife.plugin.PLPlugin;
import ru.prisonlife.util.InventoryUtil;

import java.util.List;

import static ru.prisonlife.pljobs.Main.colorize;
import static ru.prisonlife.pljobs.Main.playersSalary;

public class InventoryClick implements Listener {

    private PLPlugin plugin;
    public InventoryClick(PLPlugin main) {
        this.plugin = main;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Prisoner prisoner = PrisonLife.getPrisoner(player);

        if (event.getView().getTitle().equals(ChatColor.BOLD + "" + ChatColor.GRAY + "Шахтер") || event.getView().getTitle().equals(ChatColor.BOLD + "" + ChatColor.GREEN + "Уборщик") || event.getView().getTitle().equals(ChatColor.BOLD + "" + ChatColor.GOLD + "Повар")) {
            ItemStack item = event.getCurrentItem();
            if (item.getItemMeta().getDisplayName().equals(ChatColor.BOLD + "" + ChatColor.RED + "Уволиться и получить зарплату")) {
                player.closeInventory();

                Integer overdueAmount;

                if (prisoner.hasOverdueJobSalary()) {
                    overdueAmount = prisoner.getOverdueJobSalary();
                } else {
                    overdueAmount = 0;
                }

                Integer salary = playersSalary.get(player);

                player.getInventory().clear();
                List<ItemSlot> items = PrisonLife.getStoredInventory(prisoner.getAccountNumber());
                InventoryUtil.putItemSlots(player.getInventory(), items);

                playersSalary.remove(player);
                prisoner.setOverdueJobSalary(0);
                prisoner.setJob(Job.NONE);
                player.sendMessage(colorize(plugin.getConfig().getString("messages.leaveJob")));

                if (PrisonLife.getCurrencyManager().canPuttedMoney(player.getInventory(), overdueAmount + salary)) {
                    player.getInventory().addItem((ItemStack) PrisonLife.getCurrencyManager().createMoney(overdueAmount + salary));
                } else {
                    player.sendMessage(colorize(plugin.getConfig().getString("messages.notEnoughSlots")));
                    prisoner.setOverdueJobSalary(overdueAmount + salary);
                }
            }
        }

        if (event.getView().getTitle().equals(ChatColor.BOLD + "" + ChatColor.GRAY + "Работы")) {
            ItemStack item = event.getCurrentItem();
            if (item.getType() == Material.STONE_PICKAXE) {
                if (prisoner.getLevel() >= plugin.getConfig().getInt("jobLevels.miner")) {
                    prisoner.setJob(Job.MINER);
                    playersSalary.put(player, 0);
                    player.sendMessage(colorize(plugin.getConfig().getString("messages.joinJob")));
                    player.closeInventory();
                } else {
                    player.sendMessage(colorize(plugin.getConfig().getString("messages.notEnoughLevel")));
                    player.closeInventory();
                }
            } else if (item.getType() == Material.IRON_SHOVEL) {
                if (prisoner.getLevel() >= plugin.getConfig().getInt("jobLevels.cleaner")) {
                    prisoner.setJob(Job.CLEANER);
                    player.sendMessage(colorize(plugin.getConfig().getString("messages.joinJob")));
                    player.closeInventory();
                } else {
                    player.sendMessage(colorize(plugin.getConfig().getString("messages.notEnoughLevel")));
                    player.closeInventory();
                }
            } else if (item.getType() == Material.CAKE) {
                if (prisoner.getLevel() >= plugin.getConfig().getInt("jobLevels.cook")) {
                    prisoner.setJob(Job.COOK);
                    player.sendMessage(colorize(plugin.getConfig().getString("messages.joinJob")));
                    player.closeInventory();
                } else {
                    player.sendMessage(colorize(plugin.getConfig().getString("messages.notEnoughLevel")));
                    player.closeInventory();
                }
            }
        }

    }
}
