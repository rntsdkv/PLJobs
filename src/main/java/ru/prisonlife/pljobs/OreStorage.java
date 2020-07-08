package ru.prisonlife.pljobs;

import org.bukkit.Location;

/**
 * @author rntsdkv
 * @project PLJobs
 */

public class OreStorage {

    private Location location;
    private int count;
    private int maximum;

    public OreStorage(Location location, int count, int maximum) {
        this.location = location;
        this.count = count;
        this.maximum = maximum;
    }

    private void setMaximum(int i) {
        this.maximum = i;
    }

    private int getMaximum() {
        return maximum;
    }

    private int getCount() {
        return count;
    }

    private void setCount(int i) {
        this.count = i;
    }

    private boolean canPuttedCount(int i) {
        if (count + i > maximum) return false;
        return true;
    }

    private void putCount(int i) {
        count += i;
    }

    private Location getLocation() {
        return location;
    }

    private void setLocation(Location location) {
        this.location = location;
    }
}
