package ru.prisonlife.pljobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.UUID;

/**
 * @author rntsdkv
 * @project PLJobs
 */

public class OreStorage {

    private Location location;
    private int count;
    private int maximum;
    private UUID uuid;

    public OreStorage(Location location, int count, int maximum) {
        this.location = location;
        this.count = count;
        this.maximum = maximum;
    }

    public void setMaximum(int i) {
        this.maximum = i;
    }

    public int getMaximum() {
        return maximum;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int i) {
        this.count = i;
    }

    public boolean canPuttedCount(int i) {
        if (count + i > maximum) return false;
        return true;
    }

    public void putCount(int i) {
        count += i;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        if (uuid != null) {
            Entity entity = Bukkit.getEntity(uuid);
            entity.teleport(location);
        } else {
            ArmorStand entity = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            entity.setCustomNameVisible(true);
            entity.setCustomName("Руда\n\n" + "Количество на складе: " + count + "\nМаксимальное количество: " + maximum);
            entity.setGravity(false);
            entity.setVisible(false);
            uuid = entity.getUniqueId();
        }
    }

    public void updateText() {
        Bukkit.getEntity(uuid).setCustomName("Руда\n\n" + "Количество на складе: " + count + "\nМаксимальное количество: " + maximum);
    }
}
