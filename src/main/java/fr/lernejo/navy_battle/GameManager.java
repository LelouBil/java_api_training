package fr.lernejo.navy_battle;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.lernejo.navy_battle.pojo.GameDefinition;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class GameManager {


    private final UUID id;
    private final String url;

    @SuppressWarnings("FieldCanBeLocal")
    private final HttpClient httpClient;

    public GameManager(UUID id, String url, String remoteUrl) {
        this.id = id;
        this.url = url;
        this.httpClient = HttpClient.newHttpClient();
        if(remoteUrl != null){
            try {
                callApi(remoteUrl);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void callApi(String remoteUrl) throws IOException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(new GameDefinition(
            id.toString(), url,"I will crush you!"
        ));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(remoteUrl + "/api/game/start"))
            .setHeader("Accept","application/json")
            .setHeader("Content-Type","application/json")
            .POST(HttpRequest.BodyPublishers.ofString(data))
            .build();
        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }


    public fr.lernejo.navy_battle.pojo.GameDefinition createGame(fr.lernejo.navy_battle.pojo.GameDefinition body) {


        return new GameDefinition(id.toString(), url,"May the best code win");
    }

}
