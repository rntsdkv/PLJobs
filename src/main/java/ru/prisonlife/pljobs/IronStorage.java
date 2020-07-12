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

public class IronStorage {

    private int count;
    private int maximum;
    private Location location;
    private UUID uuid;

    public IronStorage(int count, int maximum, Location location) {
        this.count = count;
        this.maximum = maximum;
        this.location = location;
    }

    public void setLocation(Location location) {
        this.location = location;
        if (uuid != null) {
            Entity entity = Bukkit.getEntity(uuid);
            entity.teleport(location);
        } else {
            ArmorStand entity = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            entity.setCustomNameVisible(true);
            entity.setSmall(true);
            entity.setCustomName("Железо " + count + "/" + maximum);
            entity.setGravity(false);
            entity.setVisible(false);
            uuid = entity.getUniqueId();
        }
    }

    public void updateText() {
        Bukkit.getEntity(uuid).setCustomName("Железо " + count + "/" + maximum);
    }

    public Location getLocation() { return location; }

    public void setMaximum(int i) {
        this.maximum = i;
    }

    public int getMaximum() { return maximum; }

    public int getCount() {
        return count;
    }

    public void setCount(int i) {
        this.count = i;
    }

    public boolean canPuttedCount(int i) {
        if (count + i > maximum || count + i < 0) return false;
        return true;
    }

    public void putCount(int i) {
        count += i;
    }
}
