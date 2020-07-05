package ru.prisonlife.pljobs.events;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import ru.prisonlife.Job;
import ru.prisonlife.PrisonLife;
import ru.prisonlife.Prisoner;
import ru.prisonlife.plugin.PLPlugin;

import static ru.prisonlife.pljobs.Main.minerBlockValues;
import static ru.prisonlife.pljobs.Main.playersSalary;

/**
 * @author rntsdkv
 * @project PLJobs
 */

public class MinerListener implements Listener {

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

        event.setDropItems(false);

        if (!minerBlockValues.containsKey(blockType)) return;
        if (prisoner.getJob() != Job.MINER) return;

        int price = minerBlockValues.get(blockType);

        playersSalary.put(player, playersSalary.get(player) + price);
        player.getInventory().addItem(new ItemStack(block.getType(), 1));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + String.format("+%d$", price)));
    }

    @EventHandler
    public void
}
