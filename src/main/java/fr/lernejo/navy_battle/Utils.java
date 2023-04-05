package fr.lernejo.navy_battle;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.lernejo.navy_battle.pojo.FireConsequence;
import fr.lernejo.navy_battle.pojo.FireData;
import fr.lernejo.navy_battle.pojo.GameDefinition;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.util.UUID;

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

        return getBoat(height, width, size, random, dir, xPadding, yPadding);
    }

    private static Boat getBoat(int height, int width, int size, SecureRandom random, Boat.Direction dir, int minX, int minY) {
        int maxX = width - minX;

        int maxY = height - minY;

        int xPos = random.nextInt(minX, maxX);
        int yPos = random.nextInt(minY, maxY);
        return new Boat(size, xPos, yPos, dir);
    }

    HttpRequest getFireRequest(String cell, String remoteUrl) {
        return HttpRequest
            .newBuilder()
            .header("Accept", "application/json")
            .uri(URI.create(remoteUrl + "/api/game/fire?cell=" + cell))
            .GET()
            .build();
    }

    void sendCallGameRequest(UUID id, String url, String remoteUrl, HttpClient httpClient) throws IOException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        String data;
        data = mapper.writeValueAsString(new GameDefinition(
            id.toString(), url, "I will crush you!"
        ));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(remoteUrl + "/api/game/start"))
            .setHeader("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(data))
            .build();
        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    void fireAtCell(int xPos, int yPos,String remoteUrl, HttpClient httpClient, OpponentCellState[][] opponentBoard ) {
        String cell = convertPosToCell(xPos, yPos);
        HttpRequest request = getFireRequest(cell, remoteUrl);
        try {
            String body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            ObjectMapper mapper = new ObjectMapper();
            FireData data = mapper.readValue(body, FireData.class);
            if (data.consequence() == FireConsequence.HIT || data.consequence() == FireConsequence.SUNK) {
                opponentBoard[xPos][yPos] = OpponentCellState.Hit;
            } else {
                opponentBoard[xPos][yPos] = OpponentCellState.Nothing;
            }
            if (!data.shipLeft()) {
                System.out.println("J'ai gagné");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
