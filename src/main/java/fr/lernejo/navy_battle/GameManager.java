package fr.lernejo.navy_battle;

import fr.lernejo.navy_battle.pojo.FireConsequence;
import fr.lernejo.navy_battle.pojo.GameDefinition;

import java.io.IOException;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GameManager {
    private final int HEIGHT = 10;
    private final int WIDTH = 10;
    private final UUID id;
    private final String url;
    private final AtomicReference<String> remoteUrl;
    private final HttpClient httpClient;
    private final AtomicBoolean playing = new AtomicBoolean(false);
    private final NavyBoard board = new NavyBoard(WIDTH, HEIGHT);
    private final OpponentCellState[][] opponentBoard = new OpponentCellState[WIDTH][HEIGHT];
    private final Utils utils = new Utils();

    public GameManager(UUID id, String url, String remoteUrl) {
        this.id = id;
        this.url = url;
        this.remoteUrl = new AtomicReference<>(remoteUrl);
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

    public void callStartGame() {
        playing.set(true);
        try {
            utils.sendCallGameRequest(id, url, remoteUrl.get(), httpClient);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void doTurn() {
        if (!playing.get()) return;
        SecureRandom random = new SecureRandom();
        int xPos;
        int yPos;
        do {
            xPos = random.nextInt(0, WIDTH);
            yPos = random.nextInt(0, HEIGHT);
        } while (opponentBoard[xPos][yPos] != OpponentCellState.Unknown);


        if (!playing.get()) return;
        utils.fireAtCell(xPos, yPos, remoteUrl.get(), httpClient, opponentBoard);
    }


    public fr.lernejo.navy_battle.pojo.GameDefinition createGame(fr.lernejo.navy_battle.pojo.GameDefinition body) {
        playing.set(true);
        remoteUrl.set(body.url());
        return new GameDefinition(id.toString(), url, "May the best code win");
    }

    public FireConsequence handleFire(String cell) {
        try {
            if (cell.length() < 2 || cell.length() > 3) throw new NumberFormatException();
            char letter = cell.charAt(0);
            String number = cell.substring(1);
            int letterNumber = utils.letterToNumber(letter);
            int numberNumber = Integer.parseInt(number) - 1;
            FireConsequence target = board.target(letterNumber, numberNumber);
            System.out.println(board);
            if (!shipLeft()) {
                System.out.println("J'ai perdu !");
                this.playing.set(false);
            }
            return target;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid cell : " + cell, e);
        }

    }

    public boolean shipLeft() {
        return board.shipLeft();
    }
}
