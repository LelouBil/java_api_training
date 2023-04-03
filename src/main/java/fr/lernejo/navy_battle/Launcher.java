package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Launcher {
    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newSingleThreadExecutor());
            server.createContext("/ping",ex -> {
                String body = "OK";
                ex.sendResponseHeaders(200,body.length());
                try (OutputStream os = ex.getResponseBody())
                {
                    os.write(body.getBytes());
                }
            });
            server.start();
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
            System.out.println("""
                Usage: <port>
                """);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
