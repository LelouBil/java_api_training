package fr.lernejo.navy_battle;


import fr.lernejo.navy_battle.pojo.FireConsequence;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.StreamSupport;

public class NavyBoard {
    private final int width;
    private final int height;

    private final Set<Boat> boats = new HashSet<>();
    private final Boat.RenderedBoat[][] renderedBoard;

    public boolean shipLeft() {
        return boats.stream().anyMatch(b -> b.getHealth() > 0);
    }

    public NavyBoard(int width, int height) {
        this.width = width;
        this.height = height;
        renderedBoard = new Boat.RenderedBoat[width][height];
        tryPlace(5);
        tryPlace(4);
        tryPlace(3);
        tryPlace(3);
        tryPlace(2);
        System.out.println("Board : ");
        System.out.println(this);
    }

    private void tryPlace(int size) {
        Boat pos;
        do {
            pos = getRandomBoat(size);
        }
        while (!tryRender(pos));
        boats.add(pos);
    }

    private boolean tryRender(Boat boat) {
        if (StreamSupport.stream(boat.getRenderedBoats().spliterator(), false)
            .anyMatch(rb -> renderedBoard[rb.getX()][rb.getY()] != null)) {
            return false;
        }
        for (Boat.RenderedBoat renderedBoat : boat.getRenderedBoats()) {
            renderedBoard[renderedBoat.getX()][renderedBoat.getY()] = renderedBoat;
        }

        return true;
    }


    private Boat getRandomBoat(int size) {
        SecureRandom random = new SecureRandom();
        Boat.Direction dir = Boat.Direction.values()[random.nextInt(4)];
        int xPadding = 0;
        int yPadding = 0;
        if (dir == Boat.Direction.Left || dir == Boat.Direction.Right) {
            xPadding = size - 1;
        } else {
            yPadding = size - 1;
        }

        int minX = xPadding;
        int maxX = width - xPadding;

        int minY = yPadding;
        int maxY = height - yPadding;

        int xPos = random.nextInt(minX, maxX);
        int yPos = random.nextInt(minY, maxY);
        return new Boat(size, xPos, yPos, dir);
    }

    public FireConsequence target(int x, int y) {
        if (x >= width || x < 0 || y >= height || y < 0) {
            System.err.printf("Trying to target out of bounds coords (%d;%d)%n", x, y);
            return null;
        }

        Boat.RenderedBoat targeted = renderedBoard[x][y];
        if (targeted == null || targeted.isDestroyed() || targeted.getBoat().getHealth() <= 0)
            return FireConsequence.MISS;

        targeted.destroyRender();
        System.out.println(this);
        if (targeted.getBoat().getHealth() > 0) return FireConsequence.HIT;
        else return FireConsequence.SUNK;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Boat.RenderedBoat renderedBoat = renderedBoard[j][i];
                if(renderedBoat == null){
                    sb.append('.');
                }else sb.append(renderedBoat);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
