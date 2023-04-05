package fr.lernejo.navy_battle;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class RenderedBoatIterator implements Iterator<RenderedBoat> {

    private final Boat boat;
    private final AtomicReference<RenderedBoat> previous = new AtomicReference<>(null);
    private final AtomicInteger sizeCounter = new AtomicInteger(0);

    public RenderedBoatIterator(Boat boat) {
        this.boat = boat;
    }

    @Override
    public boolean hasNext() {
        return sizeCounter.get() < boat.getSize();
    }

    @Override
    public RenderedBoat next() {
        if (previous.get() == null) {
            previous.set(new RenderedBoat(boat, boat.getX(), boat.getY()));
        } else {
            int xStep = 0;
            int yStep = 0;
            switch (boat.getDirection()) {
                case Top -> yStep = 1;
                case Down -> yStep = -1;
                case Right -> xStep = 1;
                case Left -> xStep = -1;
            }

            previous.set(new RenderedBoat(
                boat,
                this.previous.get().getX() + xStep,
                this.previous.get().getY() + yStep
            ));
        }
        sizeCounter.incrementAndGet();
        return previous.get();
    }
}
