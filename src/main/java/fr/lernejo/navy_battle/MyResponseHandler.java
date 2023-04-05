package fr.lernejo.navy_battle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MyResponseHandler {
    private final int status;
    private final String contentType;
    private final String body;
    private final Runnable afterRequestEvent;

    private MyResponseHandler(int status, String contentType, String body, Runnable afterRequestEvent) {
        this.status = status;
        this.contentType = contentType;
        this.body = body;
        this.afterRequestEvent = afterRequestEvent;
    }

    public static MyResponseHandler plain(int status, String body, Runnable afterRequestEvent) {
        return new MyResponseHandler(status, "text/plain", body, afterRequestEvent);
    }

    public static MyResponseHandler json(int status, Object body, Runnable afterRequestEvent) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String content = mapper.writeValueAsString(body);
            return new MyResponseHandler(status, "application/json", content, afterRequestEvent);
        } catch (JsonProcessingException e) {
            System.err.printf("Error in response building %d, %s%n", status, body.toString());
            e.printStackTrace();
            return MyResponseHandler.status(500,afterRequestEvent);
        }
    }

    private static MyResponseHandler status(int status, Runnable afterRequestEvent) {
        return new MyResponseHandler(status, null, null, afterRequestEvent);
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

    public void afterRequest() {
        if(this.afterRequestEvent != null)
            this.afterRequestEvent.run();
    }
}
