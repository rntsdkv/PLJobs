package ru.prisonlife.pljobs.events;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.currency.CurrencyManager;
import ru.prisonlife.database.json.ItemSlot;
import ru.prisonlife.plugin.PLPlugin;
import ru.prisonlife.util.InventoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static ru.prisonlife.pljobs.Main.*;

public class JobGuiListener implements Listener {

    private final PLPlugin plugin;
    public JobGuiListener(PLPlugin plugin) {
        this.plugin = plugin;
    }

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
                    if (getWorkerCount("cleaner") - 1 == 0) {
                        taskGarbages.cancel();
                    }
                }

                prisoner.setJob(Job.NONE);
                player.sendMessage(colorize(plugin.getConfig().getString("messages.leaveJob")));

                if (PrisonLife.getCurrencyManager().canPuttedMoney(player.getInventory(), overdueAmount + salary)) {
                    InventoryUtil.putItemStacks(player.getInventory(), PrisonLife.getCurrencyManager().createMoney(overdueAmount + salary));
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

        //if (viewTitle.equals(plugin.getConfig().getString("titles.orePointQuestion"))) {
        if (viewTitle.equals("Сдача руды")) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            Inventory inventory = player.getInventory();
            if (item.getType() == Material.GREEN_STAINED_GLASS) {
                for (int x = 0; x <= inventory.getSize(); x++) {
                    ItemStack itemInventory = inventory.getItem(x);
                    if (itemInventory == null) continue;
                    if (minerBlockValues.containsKey(itemInventory.getType().name())) {
                        int amount = itemInventory.getAmount();
                        if (oreStorage.canPuttedCount(amount)) {
                            inventory.setItem(x, null);
                            oreStorage.putCount(amount);
                        } else {
                            itemInventory.setAmount(oreStorage.getMaximum() - oreStorage.getCount());
                            oreStorage.setCount(oreStorage.getMaximum());
                            break;
                        }
                    }
                }
                player.closeInventory();
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "Вы сдали руду!"));
                oreStorage.updateText();
            } else if (item.getType() == Material.RED_STAINED_GLASS) {
                //Inventory GUI = Bukkit.createInventory(null, 9, plugin.getConfig().getString("titles.orePointPart"));
                Inventory GUI = Bukkit.createInventory(null, 9, "Положите руду для сдачи");
                player.openInventory(GUI);
            }
        }

        if (viewTitle.equals("Склад железа")) {
            event.setCancelled(true);
            player.closeInventory();
            ItemStack item = event.getCurrentItem();
            Inventory inventory = player.getInventory();
            int price = plugin.getConfig().getInt("miner.iron.priceForBuyer");
            CurrencyManager currencyManager = PrisonLife.getCurrencyManager();

            if (item.getType() != Material.IRON_INGOT) return;

            if (item.getAmount() == 1) {
                if (!ironStorage.canPuttedCount(-1)) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "На складе не хватает железа!"));
                    return;
                }
                if (!currencyManager.hasMoneyEquivalent(inventory, price)) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "Недостаточно денег!"));
                    return;
                }
                currencyManager.reduceMoney(inventory, price);
                inventory.addItem(new ItemStack(Material.IRON_INGOT, 1));
                ironStorage.putCount(-1);
                ironStorage.updateText();
            } else if (item.getAmount() == 5) {
                if (!ironStorage.canPuttedCount(-5)) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "На складе не хватает железа!"));
                    return;
                }
                if (!currencyManager.hasMoneyEquivalent(inventory, price * 5)) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "Недостаточно денег!"));
                    return;
                }
                currencyManager.reduceMoney(inventory, price * 5);
                inventory.addItem(new ItemStack(Material.IRON_INGOT, 5));
                ironStorage.putCount(-5);
                ironStorage.updateText();
            } else if (item.getAmount() == 10) {
                if (!ironStorage.canPuttedCount(-10)) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "На складе не хватает железа!"));
                    return;
                }
                if (!currencyManager.hasMoneyEquivalent(inventory, price * 10)) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "Недостаточно денег!"));
                    return;
                }
                currencyManager.reduceMoney(inventory, price * 10);
                inventory.addItem(new ItemStack(Material.IRON_INGOT, 10));
                ironStorage.putCount(-10);
                ironStorage.updateText();
            }
        }

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        InventoryView view = event.getView();
        //if (!view.getTitle().equals(plugin.getConfig().getString("titles.orePointPart"))) return;
        if (!view.getTitle().equals("Положите руду для сдачи")) return;

        Inventory inventory = event.getInventory();

        for (int x = 0; x <= inventory.getSize(); x++) {
            ItemStack itemInventory = inventory.getItem(x);
            if (itemInventory == null) continue;
            if (!minerBlockValues.containsKey(itemInventory.getType().name())) {
                player.getInventory().addItem(itemInventory);
            } else {
                int amount = itemInventory.getAmount();
                if (oreStorage.canPuttedCount(amount)) {
                    oreStorage.putCount(amount);
                } else {
                    player.getInventory().addItem(itemInventory);
                }
            }
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "Вы сдали руду!"));
        oreStorage.updateText();
    }

    private void creatingGarbage() {
        if (getWorkerCount("cleaner") == 1) {
            taskGarbages = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

                if (garbageCount < getWorkerCount("cleaner") * plugin.getConfig().getInt("cleaner.garbageCountPerCleaner")) {

                    List<Integer> list = new ArrayList<>();

                    for (Integer key : cleanerPoints.keySet()) {
                        list.add(key);
                    }

                    int rand = new Random().nextInt(list.size());

                    cleanerPoints.get(list.get(rand)).getWorld().dropItem(cleanerPoints.get(list.get(rand)), new ItemStack(Material.COCOA_BEANS, 1));
                    garbageCount ++;
                }
            }, 0, plugin.getConfig().getInt("cleaner.garbageSpawnIntensity") * 20);
        }
    }
}
