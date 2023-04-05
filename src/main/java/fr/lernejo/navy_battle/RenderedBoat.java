package fr.lernejo.navy_battle;

import java.util.concurrent.atomic.AtomicBoolean;

class RenderedBoat {
    private final Boat boat;
    private final int x;
    private final int y;
    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    RenderedBoat(Boat boat, int x, int y) {
        this.boat = boat;
        this.x = x;
        this.y = y;
    }

    public Boat getBoat() {
        return boat;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void destroyRender() {
        if (destroyed.get()) {
            System.err.printf("Trying to destroy already destroyed render, pos (%d;%d) of boat %s%n", x, y, boat.toString());
            return;
        }
        boat.decrementHealth();
        this.destroyed.set(true);
    }

    public boolean isDestroyed() {
        return destroyed.get();
    }

    @Override
    public String toString() {
        return destroyed.get() ? "X" : "M";
    }
}
