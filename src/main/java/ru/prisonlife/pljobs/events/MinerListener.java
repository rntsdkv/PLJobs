package ru.prisonlife.pljobs.events;

import org.bukkit.event.Listener;
import ru.prisonlife.plugin.PLPlugin;

/**
 * @author rntsdkv
 * @project PLJobs
 */

public class MinerListener implements Listener {

    private final PLPlugin plugin;
    public MinerListener(PLPlugin plugin) {
        this.plugin = plugin;
    }
}
