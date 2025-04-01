package dev.greenn.youtubeviewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class ApiService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ApiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://www.googleapis.com/youtube/v3").build();

        this.objectMapper = objectMapper;
    }

    public String getLatestVideo(String channelId, String apiKey)  {
        String channelUrl = "https://www.googleapis.com/youtube/v3/channels?part=contentDetails&id=" + channelId + "&key=" + apiKey;



        // Step 1: Get uploads playlist ID
        String response = webClient.get().uri(channelUrl).retrieve().bodyToMono(String.class).block();
        JsonNode root = null;
        try {
            root = new ObjectMapper().readTree(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("YouTube API Response: " + response);

        String uploadsPlaylistId = root.path("items").get(0).path("contentDetails").path("relatedPlaylists").path("uploads").asText();

        // Step 2: Get latest video
        String playlistUrl = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" + uploadsPlaylistId + "&maxResults=1&key=" + apiKey;
        String videoResponse = webClient.get().uri(playlistUrl).retrieve().bodyToMono(String.class).block();

        JsonNode videoRoot = null;
        try {
            videoRoot = new ObjectMapper().readTree(videoResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String videoId = videoRoot.path("items").get(0).path("snippet").path("resourceId").path("videoId").asText();

        return  videoId;
    }

  List<Channel> fetchSubscriptions(String accessToken, String nextPageToken, List<Channel> channels) {
        try {
            // Make pageToken final for lambda usage
            final String pageToken = nextPageToken;

            String response = webClient.get()
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
                    .bodyToMono(String.class)
                    .block(); // Synchronously fetch response

            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("items");

            for (JsonNode item : items) {
                JsonNode snippet = item.path("snippet");
                String title = snippet.path("title").asText();
                String ytId = snippet.path("resourceId").path("channelId").asText();

                //channelNames.add(title);
                channels.add(new Channel(title, ytId));
            }

            // Extract nextPageToken from the response
            String newPageToken = root.path("nextPageToken").asText(null);
            if (newPageToken != null) {
                return fetchSubscriptions(accessToken, newPageToken,  channels); // Recursive call for next page
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return channels;
    }


}
