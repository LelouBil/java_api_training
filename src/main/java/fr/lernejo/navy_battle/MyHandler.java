package fr.lernejo.navy_battle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MyHandler<T> implements HttpHandler {
    private final HttpMethod httpMethod;
    private final String path;
    private final Class<T> tClass;
    private final Function<T, MyResponseHandler> handler;

    private MyHandler(HttpMethod httpMethod, String path, Class<T> tClass, Function<T, MyResponseHandler> handler) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.tClass = tClass;
        this.handler = handler;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals(httpMethod.name())) {
            InputStream requestBody = exchange.getRequestBody();
            String data = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
            try {
                computeHandler(exchange, data);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(400, 0);
                exchange.getResponseBody().close();
            } catch (RuntimeException exception) {
                exception.printStackTrace();
            }
        } else {
            exchange.sendResponseHeaders(405, 0);
            exchange.getResponseBody().close();
        }
    }

    private void computeHandler(HttpExchange exchange, String data) throws IOException {
        MyResponseHandler resp;
        System.out.printf("%s %s?%s: %s%n", httpMethod.name(), path, exchange.getRequestURI().getQuery(), data);
        if (tClass == null) {
            Map<String, String> query = getQueryMap(exchange);
            //noinspection unchecked
            resp = handler.apply((T) query);
        } else if (tClass == String.class) {
            //noinspection unchecked
            resp = handler.apply((T) data);
        } else {
            ObjectMapper mapper = new ObjectMapper();

            T req = mapper.readValue(data, tClass);
            resp = handler.apply(req);
        }
        sendResponse(exchange, resp);
        resp.afterRequest();
    }

    private static Map<String, String> getQueryMap(HttpExchange exchange) {
        return Arrays.stream(exchange
                .getRequestURI()
                .getQuery()
                .split("&")
            )
            .map(e -> e.split("="))
            .collect(Collectors.toMap(e -> e[0], e -> e[1])
            );
    }

    private static void sendResponse(HttpExchange exchange, MyResponseHandler resp) throws IOException {
        exchange.sendResponseHeaders(resp.getStatus(), resp.getBody().length());
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.add("Content-Type", resp.getContentType());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp.getBody().getBytes());
        }
        System.out.printf(">\t%d\t%s%n", resp.getStatus(), resp.getBody());
    }

    public static <T> void post(HttpServer server, String path, Class<T> tClass, Function<T, MyResponseHandler> handler) {
        server
            .createContext(path, new MyHandler<>(HttpMethod.POST, path, tClass, handler));
    }

    public static void get(HttpServer server, String path, Function<Map<String, String>, MyResponseHandler> handler) {
        server
            .createContext(path, new MyHandler<>(HttpMethod.GET, path, null, handler));
    }
}
