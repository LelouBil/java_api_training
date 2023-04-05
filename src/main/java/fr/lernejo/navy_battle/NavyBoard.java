package fr.lernejo.navy_battle;


import fr.lernejo.navy_battle.pojo.FireConsequence;

import java.util.*;
import java.util.stream.StreamSupport;

public class NavyBoard {
    private final int width;
    private final int height;

    private final Set<Boat> boats = new HashSet<>();
    private final RenderedBoat[][] renderedBoard;
    private final Utils utils = new Utils();

    public boolean shipLeft() {
        return boats.stream().anyMatch(b -> b.getHealth() > 0);
    }

    public NavyBoard(int width, int height) {
        this.width = width;
        this.height = height;
        renderedBoard = new RenderedBoat[width][height];
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
            pos = utils.getRandomBoat(height, width, size);
        }
        while (!tryRender(pos));
        boats.add(pos);
    }

    private boolean tryRender(Boat boat) {
        if (StreamSupport.stream(boat.getRenderedBoats().spliterator(), false)
            .anyMatch(rb -> renderedBoard[rb.getX()][rb.getY()] != null)) {
            return false;
        }
        for (RenderedBoat renderedBoat : boat.getRenderedBoats()) {
            renderedBoard[renderedBoat.getX()][renderedBoat.getY()] = renderedBoat;
        }

        return true;
    }


    public FireConsequence target(int x, int y) {
        if (x >= width || x < 0 || y >= height || y < 0) {
            System.err.printf("Trying to target out of bounds coords (%d;%d)%n", x, y);
            return null;
        }

        RenderedBoat targeted = renderedBoard[x][y];
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
                RenderedBoat renderedBoat = renderedBoard[j][i];
                if(renderedBoat == null){
                    sb.append('.');
                }else sb.append(renderedBoat);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
