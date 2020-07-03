package ru.prisonlife.pljobs.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.database.json.ItemSlot;
import ru.prisonlife.plugin.PLPlugin;
import ru.prisonlife.util.InventoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ru.prisonlife.pljobs.Main.*;

public class InventoryClick implements Listener {

    private PLPlugin plugin;
    public InventoryClick(PLPlugin main) {
        this.plugin = main;
    }

    final Random random = new Random();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Prisoner prisoner = PrisonLife.getPrisoner(player);

        String viewTitle = event.getView().getTitle();

        // Увольнение
        if (viewTitle.equals("Шахтер") || viewTitle.equals("Уборщик") || viewTitle.equals("Повар")) {
            event.setCancelled(true);
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

                if (prisoner.getJob() == Job.CLEANER) {
                    if (getCleanersCount() - 1 == 0) {
                        task.cancel();
                    }
                }

                prisoner.setJob(Job.NONE);
                player.sendMessage(colorize(plugin.getConfig().getString("messages.leaveJob")));

                if (PrisonLife.getCurrencyManager().canPuttedMoney(player.getInventory(), overdueAmount + salary)) {
                    for (ItemStack itemMoney : PrisonLife.getCurrencyManager().createMoney(overdueAmount + salary)) {
                        player.getInventory().addItem(itemMoney);
                    }
                    prisoner.setOverdueJobSalary(0);
                } else {
                    player.sendMessage(colorize(plugin.getConfig().getString("messages.notEnoughSlots")));
                    prisoner.setOverdueJobSalary(overdueAmount + salary);
                }
            }
        }

        // Присоединение к работам
        if (viewTitle.equals("Работы")) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item.getType() == Material.STONE_PICKAXE) {

                if (prisoner.getLevel() >= plugin.getConfig().getInt("jobLevels.miner")) {
                    prisoner.setJob(Job.MINER);
                    playersSalary.put(player, 0);

                    PrisonLife.savePlayerInventory(prisoner);

                    Inventory inventory = player.getInventory();
                    inventory.clear();
                    inventory.addItem(new ItemStack(Material.IRON_PICKAXE, 1));

                    player.sendMessage(colorize(plugin.getConfig().getString("messages.joinJob")));
                } else {
                    player.sendMessage(colorize(plugin.getConfig().getString("messages.notEnoughLevel")));
                }

                player.closeInventory();
            } else if (item.getType() == Material.IRON_SHOVEL) {

                if (prisoner.getLevel() >= plugin.getConfig().getInt("jobLevels.cleaner")) {
                    prisoner.setJob(Job.CLEANER);
                    playersSalary.put(player, 0);

                    PrisonLife.savePlayerInventory(prisoner);

                    player.getInventory().clear();
                    player.getInventory().addItem(new ItemStack(Material.IRON_SHOVEL, 1));

                    creatingGarbage();

                    player.sendMessage(colorize(plugin.getConfig().getString("messages.joinJob")));
                } else {
                    player.sendMessage(colorize(plugin.getConfig().getString("messages.notEnoughLevel")));
                }

                player.closeInventory();
            } else if (item.getType() == Material.CAKE) {

                if (prisoner.getLevel() >= plugin.getConfig().getInt("jobLevels.cook")) {
                    prisoner.setJob(Job.COOK);
                    playersSalary.put(player, 0);
                    player.sendMessage(colorize(plugin.getConfig().getString("messages.joinJob")));
                } else {
                    player.sendMessage(colorize(plugin.getConfig().getString("messages.notEnoughLevel")));
                }

                player.closeInventory();
            }
        }

    }

    private void creatingGarbage() {
        if (getCleanersCount() == 1) {
            task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                @Override
                public void run() {

                    if (garbageCount < getCleanersCount() * plugin.getConfig().getInt("cleaner.garbageCountPerCleaner")) {

                        List<Integer> list = new ArrayList<>();

                        for (Integer key : cleanerPoints.keySet()) {
                            list.add(key);
                        }

                        int rand = random.nextInt(list.size());

                        cleanerPoints.get(list.get(rand)).getWorld().dropItem(cleanerPoints.get(list.get(rand)), new ItemStack(Material.COCOA_BEANS, 1));
                        garbageCount ++;
                    }
                }
            }, 0, plugin.getConfig().getInt("cleaner.garbageSpawnIntensity") * 20);
        }
    }
}
