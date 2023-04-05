package fr.lernejo.navy_battle;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

final class Boat {
    private final int size;
    private final int startX;
    private final int startY;
    private final Direction direction;
    private final AtomicInteger health;


    Boat(int size, int startX, int startY, Direction direction) {
        this.size = size;
        this.startX = startX;
        this.startY = startY;
        this.direction = direction;
        health = new AtomicInteger(size);
    }

    @Override
    public String toString() {
        return "Boat[" +
            "size=" + size + ", " +
            "startX=" + startX + ", " +
            "startY=" + startY + ", " +
            "direction=" + direction + ']';
    }

    public int getHealth() {
        return health.get();
    }

    public void decrementHealth(){
        health.decrementAndGet();
    }

    public int getX() {
        return startX;
    }

    public int getY() {
        return startY;
    }


    public Iterable<RenderedBoat> getRenderedBoats(){
        return this::getRenderedBoatsIteratorInternal;
    }



    private Iterator<RenderedBoat> getRenderedBoatsIteratorInternal() {
        return new RenderedBoatIterator(this);
    }

    public int getSize() {
        return size;
    }

    public Direction getDirection() {
        return direction;
    }


    enum Direction {
        Top,
        Down,
        Left,
        Right

    }

}
