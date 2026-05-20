package com.ftn.model;

import java.io.Serializable;

import com.ftn.model.enums.CapacityLevel;

public class StationCapacity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Location location;
    private CapacityLevel level;
    private int activePumps;
    private int totalPumps;

    public StationCapacity() {
    }

    public StationCapacity(Location location, CapacityLevel level, int activePumps, int totalPumps) {
        this.location = location;
        this.level = level;
        this.activePumps = activePumps;
        this.totalPumps = totalPumps;
    }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public CapacityLevel getLevel() { return level; }
    public void setLevel(CapacityLevel level) { this.level = level; }

    public int getActivePumps() { return activePumps; }
    public void setActivePumps(int activePumps) { this.activePumps = activePumps; }

    public int getTotalPumps() { return totalPumps; }
    public void setTotalPumps(int totalPumps) { this.totalPumps = totalPumps; }

    @Override
    public String toString() {
        return "StationCapacity[" + (location != null ? location.getCode() : "?")
                + " = " + level + " (" + activePumps + "/" + totalPumps + ")]";
    }
}
