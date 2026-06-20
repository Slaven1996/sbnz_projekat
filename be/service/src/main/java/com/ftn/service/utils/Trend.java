package com.ftn.service.utils;

public class Trend {

    private final int direction;

    private int ticksLeft;

    public Trend(int direction, int ticksLeft) {
        this.direction = direction;
        this.ticksLeft = ticksLeft;
    }

    public int direction() {
        return direction;
    }

    public boolean isActive() {
        return ticksLeft > 0;
    }

    public void consumeTick() {
        if (ticksLeft > 0) {
            ticksLeft--;
        }
    }
}
