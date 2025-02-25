package dev.greenn.youtubeviewer;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@Controller
public class UserController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OidcUser user, Model model) {
        if (user != null) {
            System.out.println("User authenticated: " + user.getFullName());
            System.out.println("Email: " + user.getEmail());
            System.out.println("ID Token: " + user.getIdToken().getTokenValue());

            model.addAttribute("name", user.getFullName());
            model.addAttribute("email", user.getEmail());
        } else {
            System.out.println("User is NULL, authentication failed.");
        }
        return "index.html";
    }

    @GetMapping("/subscriptions")
    public String subscriptions(@AuthenticationPrincipal OidcUser user, Model model) {
        if (user != null) {
            // Use the access token to call the YouTube API
            String accessToken = user.getIdToken().getTokenValue();
            // Pass access token to service or make API calls
            model.addAttribute("subscriptions", fetchSubscriptions(accessToken));
        }
        return "subscriptions.html";
    }

    private String fetchSubscriptions(String accessToken) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://www.googleapis.com/youtube/v3/subscriptions")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();

        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("part", "snippet")
                        .queryParam("mine", "true")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return response;
    }
}