package fr.lernejo.navy_battle;

import java.security.SecureRandom;

public class Utils {
    public String convertPosToCell(int xPos, int yPos) {
        if (xPos < 0 || xPos > 9 || yPos < 0 || yPos > 9) {
            throw new NumberFormatException("Invalid pos : (" + xPos + ";" + yPos + ")");
        }
        return (char) ('A' + xPos) + String.valueOf(yPos + 1);
    }

    public int letterToNumber(char letter) throws NumberFormatException {
        int letterNumber = letter - 'A';
        if (letterNumber < 0 || letterNumber > 9) {
            throw new NumberFormatException("Invalid letter pos : " + letter);
        }
        return letterNumber;
    }

    public Boat getRandomBoat(int height, int width, int size) {
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
}
