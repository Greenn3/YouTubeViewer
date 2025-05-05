package dev.greenn.youtubeviewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class YouTubeDataFetcher {

    private final RestClient restClient;


    public YouTubeDataFetcher(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://www.googleapis.com/youtube/v3")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }

    List<Channel> fetchSubscriptions(String accessToken, String nextPageToken, List<Channel> channels) {
        try {
            final String pageToken = nextPageToken;

            String response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/subscriptions")
                                .queryParam("part", "snippet")
                                .queryParam("mine", "true")
                                .queryParam("maxResults", "50");

                        if (pageToken != null) { // Add pageToken if it exists
                            builder.queryParam("pageToken", pageToken);
                        }

                        return builder.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("items");


            for (JsonNode item : items) {
                JsonNode snippet = item.path("snippet");
                String title = snippet.path("title").asText();
                String ytId = snippet.path("resourceId").path("channelId").asText();

                channels.add(new Channel(title, ytId));
            }


            String newPageToken = root.path("nextPageToken").asText(null);
            if (newPageToken != null) {
                return fetchSubscriptions(accessToken, newPageToken,  channels); // Recursive call for next page
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return channels;
    }

    public List<String> getLatestVideo(String channelId, String apiKey)  {

List<String> videoIdList = new ArrayList<>();
        // Step 1: Get uploads playlist ID
        JsonNode root = restClient
                .get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                    .path("/search")
                            .queryParam("part", "id")
                            .queryParam("channelId", channelId)
                            .queryParam("maxResults", "3")
                            .queryParam("key", apiKey);
                    return builder.build();
                })
                .retrieve()
                .body(JsonNode.class);

JsonNode items = root.get("items");
        for(JsonNode item : items){

            videoIdList.add(item.path("id").path("videoId").asText());
        }
return videoIdList;

    }

    public List<String> getVideosAddedAfter(String channelId, String apiKey, String days)  {
Instant date = Instant.now().minus(Integer.parseInt(days), ChronoUnit.DAYS);
        List<String> videoIdList = new ArrayList<>();
        // Step 1: Get uploads playlist ID
        JsonNode root = restClient
                .get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/search")
                            .queryParam("part", "id")
                            .queryParam("channelId", channelId)
                            .queryParam("maxResults", "3")
                            .queryParam("key", apiKey)
                            .queryParam("type", "video")
                            .queryParam("publishedAfter", date);
                    return builder.build();
                })
                .retrieve()
                .body(JsonNode.class);

        JsonNode items = root.get("items");
        for(JsonNode item : items){

            videoIdList.add(item.path("id").path("videoId").asText());
        }
        return videoIdList;

    }




}


