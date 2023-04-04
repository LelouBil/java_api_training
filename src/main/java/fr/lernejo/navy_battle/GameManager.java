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

public class GameManager {


    public static final int HEIGHT = 10;
    public static final int WIDTH = 10;
    private final UUID id;
    private final String url;
    private String remoteUrl;

    @SuppressWarnings("FieldCanBeLocal")
    private final HttpClient httpClient;

    private enum OpponentCellState {
        Hit,
        Nothing,
        Unknown
    }

    private final NavyBoard board = new NavyBoard(WIDTH, HEIGHT);
    private final OpponentCellState[][] opponentBoard = new OpponentCellState[WIDTH][HEIGHT];

    public GameManager(UUID id, String url, String remoteUrl) {
        this.id = id;
        this.url = url;
        this.remoteUrl = remoteUrl;
        this.httpClient = HttpClient.newHttpClient();
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                opponentBoard[i][j] = OpponentCellState.Unknown;
            }
        }
        if (remoteUrl != null) {
            callStartGame();

        }
    }

    private void callStartGame() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String data;
            data = mapper.writeValueAsString(new GameDefinition(
                id.toString(), url, "I will crush you!"
            ));
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(remoteUrl + "/api/game/start"))
                .setHeader("Accept", "application/json")
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private void doTurn() {
        SecureRandom random = new SecureRandom();
        int xPos;
        int yPos;
        do {
            xPos = random.nextInt(WIDTH);
            yPos = random.nextInt(HEIGHT);
        } while (opponentBoard[xPos][yPos] != OpponentCellState.Unknown);


        fireAtCell(xPos, yPos);
    }

    private void fireAtCell(int xPos, int yPos) {
        String cell = convertPosToCell(xPos, yPos);
        HttpRequest request = HttpRequest
            .newBuilder()
            .uri(URI.create(remoteUrl + "/api/game/fire?cell=" + cell))
            .GET()
            .build();
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

    private String convertPosToCell(int xPos, int yPos) {
        return null; //todo
    }


    public fr.lernejo.navy_battle.pojo.GameDefinition createGame(fr.lernejo.navy_battle.pojo.GameDefinition body) {
        remoteUrl = body.url();
        //todo fire back after request end

        return new GameDefinition(id.toString(), url, "May the best code win");
    }

    public FireConsequence handleFire(String cell) {

        try {
            if (cell.length() != 2) throw new NumberFormatException();
            char letter = cell.charAt(0);
            char number = cell.charAt(1);
            int letterNumber = letterToNumber(letter);
            int numberNumber = Integer.parseInt(String.valueOf(number)) - 1;
            FireConsequence target = board.target(letterNumber, numberNumber);
            if (!shipLeft()) {
                System.out.println("J'ai perdu !");
            }
            //todo fire back
            return target;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid cell : " + cell);
        }

    }

    private int letterToNumber(char letter) throws NumberFormatException {
        int letterNumber;
        switch (letter) {
            case 'A' -> letterNumber = 0;
            case 'B' -> letterNumber = 1;
            case 'C' -> letterNumber = 2;
            case 'D' -> letterNumber = 3;
            case 'E' -> letterNumber = 4;
            case 'F' -> letterNumber = 5;
            default -> throw new NumberFormatException();
        }
        return letterNumber;
    }

    public boolean shipLeft() {
        return board.shipLeft();
    }
}
