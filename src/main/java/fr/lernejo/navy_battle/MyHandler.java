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
import java.util.function.Function;

public class MyHandler<T> implements HttpHandler {
    private final Method method;
    private final String path;
    private final Class<T> tClass;
    private final Function<T, MyResponseHandler> handler;

    private MyHandler(Method method, String path, Class<T> tClass, Function<T, MyResponseHandler> handler) {
        this.method = method;
        this.path = path;
        this.tClass = tClass;
        this.handler = handler;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals(method.name())) {
            InputStream requestBody = exchange.getRequestBody();
            String data = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
            MyResponseHandler resp;
            try {
                System.out.printf("%s %s: %s%n", method.name(),path,data);
                if (tClass == String.class) {
                    //noinspection unchecked
                    resp = handler.apply((T) data);
                } else {
                    ObjectMapper mapper = new ObjectMapper();

                    T req = mapper.readValue(data, tClass);
                    resp = handler.apply(req);
                }
                exchange.sendResponseHeaders(resp.status, resp.body.length());
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.add("Content-Type", resp.contentType);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp.body.getBytes());
                }
                System.out.printf(">\t%d\t%s%n", resp.status,resp.body);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(400, 0);
                exchange.getResponseBody().close();
            }
        } else {
            exchange.sendResponseHeaders(405, 0);
            exchange.getResponseBody().close();
        }
    }

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }

    public static class MyResponseHandler {
        private final int status;
        private final String contentType;
        private final String body;

        private MyResponseHandler(int status, String contentType, String body) {
            this.status = status;
            this.contentType = contentType;
            this.body = body;
        }

        public static MyResponseHandler plain(int status, String body) {
            return new MyResponseHandler(status, "text/plain", body);
        }

        public static MyResponseHandler json(int status, Object body) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String content = mapper.writeValueAsString(body);
                return new MyResponseHandler(status, "application/json", content);
            } catch (JsonProcessingException e) {
                System.err.printf("Error in response building %d, %s%n", status, body.toString());
                e.printStackTrace();
                return MyResponseHandler.status(500);
            }
        }

        private static MyResponseHandler status(int status) {
            return new MyResponseHandler(status, null, null);
        }
    }

    public static <T> void attach(HttpServer server, String path, Method method, Class<T> tClass, Function<T, MyResponseHandler> handler) {
        server
            .createContext(path, new MyHandler<>(method, path, tClass, handler));
    }
}
