package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpServer;
import fr.lernejo.navy_battle.pojo.FireConsequence;
import fr.lernejo.navy_battle.pojo.FireData;
import fr.lernejo.navy_battle.pojo.GameDefinition;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.Executors;

public class Launcher {
    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            String url = null;
            if (args.length >= 2) url = args[1];
            startGame(port, url);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("""
                Usage: <port>
                """);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void startGame(int port, String remoteUrl) throws IOException {
        HttpServer server = createServer(port);
        UUID id = UUID.randomUUID();
        GameManager manager = new GameManager(id, "http://localhost:%d".formatted(port), remoteUrl);

        MyHandler.post(server,
            "/api/game/start",
            GameDefinition.class,
            sgb -> MyResponseHandler
                .json(202, manager.createGame(sgb), manager::doTurn)
        );

        addFireHandler(server, manager);


        server.start();
    }

    private static void addFireHandler(HttpServer server, GameManager manager) {
        MyHandler.get(server,
            "/api/game/fire",
            query -> {
                String cell = query.get("cell");
                if (cell != null) {
                    try {
                        FireConsequence consequence = manager.handleFire(cell);
                        return MyResponseHandler.json(200, new FireData(consequence, manager.shipLeft()), manager::doTurn);
                    } catch (IllegalArgumentException exception) {
                        exception.printStackTrace();
                        return MyResponseHandler.plain(400, "", null);
                    }
                } else {
                    return MyResponseHandler.plain(400, "", null);
                }
            }
        );
    }

    private static HttpServer createServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.createContext("/ping", ex -> {
            String body = "OK";
            ex.sendResponseHeaders(200, body.length());
            try (OutputStream os = ex.getResponseBody()) {
                os.write(body.getBytes());
            }
        });
        return server;
    }
}
