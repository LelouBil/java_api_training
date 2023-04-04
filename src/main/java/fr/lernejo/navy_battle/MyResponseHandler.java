package fr.lernejo.navy_battle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MyResponseHandler {
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

    public int getStatus() {
        return status;
    }

    public String getContentType() {
        return contentType;
    }

    public String getBody() {
        return body;
    }
}
