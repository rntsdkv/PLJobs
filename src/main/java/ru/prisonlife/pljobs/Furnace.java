package ru.prisonlife.pljobs;

import static ru.prisonlife.pljobs.Main.minerFurnaces;

/**
 * @author 79869
 * @project PLJobs
 */

public class Furnace {

    private int x;
    private int y;
    private int z;

    public Furnace(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() { return x; }

    public int getY() { return y; }

    public int getZ() { return z; }

    public boolean exists() {
        for (Furnace f : minerFurnaces) {
            if (x == f.getX() && y == f.getY() && z == f.getZ()) return true;
        }
        return false;
    }

    public int find() {
        for (int i = 0; i <= minerFurnaces.size(); i++) {
            Furnace f = minerFurnaces.get(i);
            if (x == f.getX() && y == f.getY() && z == f.getZ()) return i;
        }
        return -1;
    }

}
