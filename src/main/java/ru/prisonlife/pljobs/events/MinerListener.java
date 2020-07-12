package ru.prisonlife.pljobs.events;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.pljobs.Furnace;
import ru.prisonlife.plugin.PLPlugin;

import java.util.HashMap;

import static ru.prisonlife.pljobs.Main.*;
import static ru.prisonlife.pljobs.commands.Miner.furnacesPlayer;

/**
 * @author rntsdkv
 * @project PLJobs
 */

public class MinerListener implements Listener {

    public static HashMap<Player, Integer> oreMelting = new HashMap<>();

    private final PLPlugin plugin;
    public MinerListener(PLPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Prisoner prisoner = PrisonLife.getPrisoner(player);

        Block block = event.getBlock();
        String blockType = block.getType().name();

        if (block.getType() == Material.FURNACE) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            Furnace furnace = new Furnace(x, y, z);
            if (!furnace.exists()) return;

            minerFurnaces.remove(furnace.find());
            player.sendMessage(colorize("&l&6Вы убрали плавильню!"));
            return;
        }

        event.setDropItems(false);

        if (!minerBlockValues.containsKey(blockType)) return;
        if (prisoner.getJob() != Job.MINER) return;

        int price = minerBlockValues.get(blockType);

        playersSalary.put(player, playersSalary.get(player) + price);
        player.getInventory().addItem(new ItemStack(block.getType(), 1));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + String.format("+%d$", price)));
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        Prisoner prisoner = PrisonLife.getPrisoner(player);

        if (prisoner.getJob() != Job.MINER) return;

        Location location = player.getLocation();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        if (orePoint.getX() == x && orePoint.getY() == y && orePoint.getZ() == z) {
            Inventory inventory = newInventory();
            player.openInventory(inventory);
            return;
        }

        Location oreStorageLocation = oreStorage.getLocation();

        if (oreStorageLocation.getBlockX() == x && oreStorageLocation.getBlockY() == y && oreStorageLocation.getBlockZ() == z) {
            int count = plugin.getConfig().getInt("miner.metal.forOne");
            if (oreStorage.getCount() == 0) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "На складе нет руды!"));
                return;
            } else if (oreStorage.getCount() - count >= 0) {
                if (player.getInventory().contains(Material.IRON_ORE)) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "У вас уже есть руда!"));
                    return;
                }
                player.getInventory().addItem(new ItemStack(Material.IRON_ORE, count));
                oreStorage.putCount(-5);
                oreStorage.updateText();
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "Теперь переплавьте руду!"));
                return;
            } else {
                if (player.getInventory().contains(Material.IRON_ORE)) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "У вас уже есть руда!"));
                    return;
                }
                player.getInventory().addItem(new ItemStack(Material.IRON_ORE, oreStorage.getCount()));
                oreStorage.setCount(0);
                oreStorage.updateText();
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "Теперь переплавьте руду!"));
                return;
            }

        }
    }

    private Inventory newInventory() {
        //Inventory inventory = Bukkit.createInventory(null, 9, plugin.getConfig().getString("titles.orePointQuestion"));
        Inventory inventory = Bukkit.createInventory(null, 9, "Сдача руды");

        ItemStack yes = new ItemStack(Material.GREEN_STAINED_GLASS);
        ItemMeta yesMeta = yes.getItemMeta();
        yesMeta.setDisplayName(ChatColor.BLACK + "" + ChatColor.GREEN + "Сдать всю руду");
        yes.setItemMeta(yesMeta);
        inventory.setItem(2, yes);

        ItemStack no = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta noMeta = no.getItemMeta();
        noMeta.setDisplayName(ChatColor.BLACK + "" + ChatColor.GREEN + "Сдать часть руды");
        no.setItemMeta(noMeta);
        inventory.setItem(6, no);

        return inventory;
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) throws InterruptedException {
        Player player = event.getPlayer();
        Prisoner prisoner = PrisonLife.getPrisoner(player);

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();

        Location location = player.getLocation();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        if (block.getType() != Material.FURNACE) return;

        if (furnacesPlayer.contains(player)) {
            event.setCancelled(true);
            furnacesPlayer.remove(player);

            Furnace furnace = new Furnace(x, y, z);
            if (!furnace.exists()) {
                minerFurnaces.add(furnace);
                player.sendMessage(colorize("&l&6Вы установили плавильню!"));
            }
            return;
        }

        if (minerFurnaces.contains(new Location(location.getWorld(), x, y, z))) {
            event.setCancelled(true);
            if (prisoner.getJob() != Job.MINER) return;

            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getType() != Material.IRON_ORE) return;

            if (oreMelting.containsKey(player)) return;

            item.setAmount(item.getAmount() - 1);
            oreMelting.put(player, 6);

            while (true) {
                oreMelting.replace(player, oreMelting.get(player) - 1);
                if (oreMelting.get(player) == 0) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "IIIII"));
                    break;
                }
                else if (oreMelting.get(player) == 5) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.WHITE + "IIIII"));
                else if (oreMelting.get(player) == 4) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "I" + ChatColor.WHITE + "IIII"));
                else if (oreMelting.get(player) == 3) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "II" + ChatColor.WHITE + "III"));
                else if (oreMelting.get(player) == 2) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "III" + ChatColor.WHITE + "II"));
                else if (oreMelting.get(player) == 1) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "IIII" + ChatColor.WHITE + "I"));
                Thread.sleep(1000);
            }

            oreMelting.remove(player);
            player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 1));
        }
    }
}
