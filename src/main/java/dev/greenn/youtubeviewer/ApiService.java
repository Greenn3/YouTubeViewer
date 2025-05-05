package dev.greenn.youtubeviewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class ApiService {
    private final WebClient webClient;


    public ApiService(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://www.googleapis.com/youtube/v3")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }
    public List<String> getLatestVideoIds(List<String> channelIds, String apiKey) {
        List<String> allVideoIds = new ArrayList<>();

        for (String channelId : channelIds) {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("part", "snippet");
            params.add("channelId", channelId);
            params.add("maxResults", "3");
            params.add("order", "date");
            params.add("key", apiKey);

            String response = fetchYoutubeData("/search", params).block();

            try {
                JsonNode root = new ObjectMapper().readTree(response);
                for (JsonNode item : root.path("items")) {
                    String videoId = item.path("id").path("videoId").asText();
                    allVideoIds.add(videoId);
                }
            } catch (Exception e) {
                e.printStackTrace(); // handle better in prod
            }
        }

        return allVideoIds;
    }

    public Mono<String> fetchYoutubeData(String path, MultiValueMap<String, String> queryParams) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParams(queryParams)
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class);
    }
//    public Mono<String> getLatestVideo2(String channelId, String apiKey) {
//        // Step 1: Get uploads playlist ID
//        MultiValueMap<String, String> channelParams = new LinkedMultiValueMap<>();
//        channelParams.add("part", "contentDetails");
//        channelParams.add("id", channelId);
//        channelParams.add("key", apiKey);
//
//        return fetchYoutubeData("/channels", channelParams)
//                .flatMap(channelResponse -> {
//                    try {
//                        JsonNode root = objectMapper.readTree(channelResponse);
//                        String uploadsPlaylistId = root.path("items").get(0)
//                                .path("contentDetails")
//                                .path("relatedPlaylists")
//                                .path("uploads")
//                                .asText();
//
//                        // Step 2: Get latest video from uploads playlist
//                        MultiValueMap<String, String> playlistParams = new LinkedMultiValueMap<>();
//                        playlistParams.add("part", "snippet");
//                        playlistParams.add("playlistId", uploadsPlaylistId);
//                        playlistParams.add("maxResults", "1");
//                        playlistParams.add("key", apiKey);
//
//                        return fetchYoutubeData("/playlistItems", playlistParams);
//                    } catch (JsonProcessingException e) {
//                        return Mono.error(new RuntimeException("Failed to parse channel response", e));
//                    }
//                })
//                .map(videoResponse -> {
//                    try {
//                        JsonNode videoRoot = objectMapper.readTree(videoResponse);
//                        return videoRoot.path("items").get(0)
//                                .path("snippet")
//                                .path("resourceId")
//                                .path("videoId")
//                                .asText();
//                    } catch (JsonProcessingException e) {
//                        throw new RuntimeException("Failed to parse video response", e);
//                    }
//                });
//    }
//


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
ObjectMapper objectMapper = new ObjectMapper();
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
