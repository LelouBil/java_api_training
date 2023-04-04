package fr.lernejo.navy_battle;

import java.util.Iterator;

final class Boat {
    private final int size;
    private final int startX;
    private final int startY;
    private final Direction direction;
    private int health;


    Boat(int size, int startX, int startY, Direction direction) {
        this.size = size;
        this.startX = startX;
        this.startY = startY;
        this.direction = direction;
        health = size;
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
        return health;
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
        return new Iterator<>() {

            RenderedBoat previous = null;
            int sizeCounter = 0;

            @Override
            public boolean hasNext() {
                return sizeCounter < Boat.this.size;
            }

            @Override
            public RenderedBoat next() {
                if (previous == null) {
                    previous = new RenderedBoat(Boat.this, Boat.this.startX, Boat.this.startY);
                } else {
                    int xStep = 0;
                    int yStep = 0;
                    switch (direction) {
                        case Top -> yStep = 1;
                        case Down -> yStep = -1;
                        case Right -> xStep = 1;
                        case Left -> xStep = -1;
                    }

                    previous = new RenderedBoat(
                        Boat.this,
                        this.previous.x + xStep,
                        this.previous.y + yStep
                    );
                }
                sizeCounter++;
                return previous;
            }
        };
    }


    enum Direction {
        Top,
        Down,
        Left,
        Right

    }

    static class RenderedBoat {
        private Boat boat;
        private final int x;
        private final int y;
        private boolean destroyed = false;

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
            if(destroyed){
                System.err.printf("Trying to destroy already destroyed render, pos (%d;%d) of boat %s%n", x,y,boat.toString());
                return;
            }
            boat.health--;
            this.destroyed = true;
        }

        public boolean isDestroyed() {
            return destroyed;
        }

        @Override
        public String toString() {
            return destroyed ? "X" : "M";
        }
    }
}
