package ru.prisonlife.pljobs.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.plugin.PLPlugin;

import java.util.ArrayList;
import java.util.List;

import static ru.prisonlife.pljobs.Main.colorize;
import static ru.prisonlife.pljobs.Main.playersSalary;

public class GoWork implements CommandExecutor {

    private PLPlugin plugin;
    public GoWork(PLPlugin main) {
        this.plugin = main;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(plugin.getConfig().getString("messages.wrongSender"));
            return true;
        }

        Player player = (Player) commandSender;

        if (strings.length == 0) {
            openWorksGUI(player);
        } else if (strings.length == 1) {
            if (strings[0].equals("miner") || strings[0].equals("cook") || strings[0].equals("cleaner")) {
                openWorksGUI(player);
            } else {
                player.sendMessage(colorize(plugin.getConfig().getString("messages.wrongCommandArguments")));
                return false;
            }
        } else {
            player.sendMessage(colorize(plugin.getConfig().getString("messages.wrongCommandArguments")));
            return false;
        }
        return true;
    }

    private void openWorksGUI(Player player) {
        Prisoner prisoner = PrisonLife.getPrisoner(player);
        if (prisoner.getJob() == Job.NONE) {
            Inventory inv = Bukkit.createInventory(player, 9, ChatColor.BOLD + "" + ChatColor.GRAY + "Работы");
            inv.setItem(3, miner(prisoner));
            inv.setItem(5, cleaner(prisoner));
            inv.setItem(7, cook(prisoner));
            player.openInventory(inv);
        } else {
            Inventory inv = null;
            if (prisoner.getJob() == Job.MINER) {
                inv = Bukkit.createInventory(player, 9, ChatColor.BOLD + "" + ChatColor.GRAY + "Шахтер");
            } else if (prisoner.getJob() == Job.CLEANER) {
                inv = Bukkit.createInventory(player, 9, ChatColor.BOLD + "" + ChatColor.GREEN + "Уборщик");
            } else if (prisoner.getJob() == Job.COOK) {
                inv = Bukkit.createInventory(player, 9, ChatColor.BOLD + "" + ChatColor.GOLD + "Повар");
            }

            ItemStack work = new ItemStack(Material.RED_STAINED_GLASS, 1);
            ItemMeta workMeta = work.getItemMeta();
            workMeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.RED + "Уволиться и получить зарплату");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + playersSalary.get(player).toString() + "$ + " + ChatColor.RED + prisoner.getOverdueJobSalary() + "$");
            workMeta.setLore(lore);
            work.setItemMeta(workMeta);
            inv.setItem(5, work);
            player.openInventory(inv);
        }

    }

    private ItemStack miner(Prisoner prisoner) {
        ItemStack miner = new ItemStack(Material.STONE_PICKAXE, 1);
        ItemMeta minerMeta = miner.getItemMeta();
        minerMeta.setDisplayName(ChatColor.GRAY + "Шахтер");
        List<String> lore = new ArrayList<>();
        if (prisoner.getLevel() >= plugin.getConfig().getInt("jobLevels.miner")) {
            lore.add(ChatColor.GREEN + "Устроиться на работу шахтером");
        } else {
            lore.add(ChatColor.RED + "Чтобы устроиться на эту работу, нужно иметь " + plugin.getConfig().getString("jobLevels.miner") + "+ уровень");
        }
        minerMeta.setLore(lore);
        miner.setItemMeta(minerMeta);
        return miner;
    }

    private ItemStack cleaner(Prisoner prisoner) {
        ItemStack cleaner = new ItemStack(Material.IRON_SHOVEL, 1);
        ItemMeta cleanerMeta = cleaner.getItemMeta();
        cleanerMeta.setDisplayName(ChatColor.GREEN + "Уборщик");
        List<String> lore = new ArrayList<>();
        if (prisoner.getLevel() >= plugin.getConfig().getInt("jobLevels.cleaner")) {
            lore.add(ChatColor.GREEN + "Устроиться на работу уборщиком");
        } else {
            lore.add(ChatColor.RED + "Чтобы устроиться на эту работу, нужно иметь " + plugin.getConfig().getString("jobLevels.cleaner") + "+ уровень");
        }
        cleanerMeta.setLore(lore);
        cleaner.setItemMeta(cleanerMeta);
        return cleaner;
    }

    private ItemStack cook(Prisoner prisoner) {
        ItemStack cook = new ItemStack(Material.CAKE, 1);
        ItemMeta cookMeta = cook.getItemMeta();
        cookMeta.setDisplayName(ChatColor.GOLD + "Повар");
        List<String> lore = new ArrayList<>();
        if (prisoner.getLevel() >= plugin.getConfig().getInt("jobLevels.cook")) {
            lore.add(ChatColor.GREEN + "Устроиться на работу поваром");
        } else {
            lore.add(ChatColor.RED + "Чтобы устроиться на эту работу, нужно иметь " + plugin.getConfig().getString("jobLevels.cook") + "+ уровень");
        }
        cookMeta.setLore(lore);
        cook.setItemMeta(cookMeta);
        return cook;
    }
}
