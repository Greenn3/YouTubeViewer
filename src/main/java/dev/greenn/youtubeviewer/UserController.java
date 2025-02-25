package dev.greenn.youtubeviewer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UserController {


    private final OAuth2AuthorizedClientService authorizedClientService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public UserController(OAuth2AuthorizedClientService authorizedClientService, WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.authorizedClientService = authorizedClientService;
        this.webClient = webClientBuilder.baseUrl("https://www.googleapis.com/youtube/v3").build();

        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public String home( Model model, OAuth2AuthenticationToken token) {
        String videoId = "c8iZksB-d0I"; // ID wideo YouTube
        model.addAttribute("videoId", videoId);
        model.addAttribute("name", token.getPrincipal().getAttribute("name"));
        model.addAttribute("email", token.getPrincipal().getAttribute("email"));
        model.addAttribute("photo", token.getPrincipal().getAttribute("picture"));
        return "index";
    }

    @RequestMapping("/user")
    public Principal user(Principal user){
        return user;
    }
    @GetMapping("/subscriptions")
    public Mono<ResponseEntity<List<String>>> getSubscriptions(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        String accessToken = client.getAccessToken().getTokenValue();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/subscriptions")
                        .queryParam("part", "snippet")
                        .queryParam("mine", "true")
                        .queryParam("maxResults", "1000")
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    List<String> channelNames = extractChannelNames(response);
                    return ResponseEntity.ok().body(channelNames);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(List.of("Error fetching subscriptions: " + e.getMessage()))));
    }

    private List<String> extractChannelNames(String jsonResponse) {
        List<String> channelNames = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode items = root.path("items");

            for (JsonNode item : items) {
                JsonNode snippet = item.path("snippet");
                String title = snippet.path("title").asText();
                channelNames.add(title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channelNames;
    }



    @GetMapping("/profile")
    public String showProfile(OAuth2AuthenticationToken token, Model model){
        model.addAttribute("name", token.getPrincipal().getAttribute("name"));
        model.addAttribute("email", token.getPrincipal().getAttribute("email"));
        model.addAttribute("photo", token.getPrincipal().getAttribute("picture"));

        return "profile";
    }

}