package dev.greenn.youtubeviewer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller

public class UserController {


    private final OAuth2AuthorizedClientService authorizedClientService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final  DataService dataService;
    private final CategoryRepository categoryRepository;
    private final ChannelRepository channelRepository;

    public UserController(OAuth2AuthorizedClientService authorizedClientService, WebClient.Builder webClientBuilder, ObjectMapper objectMapper, DataService dataService, CategoryRepository categoryRepository, ChannelRepository channelRepository) {
        this.authorizedClientService = authorizedClientService;
        this.webClient = webClientBuilder.baseUrl("https://www.googleapis.com/youtube/v3").build();

        this.objectMapper = objectMapper;
        this.dataService = dataService;
        this.categoryRepository = categoryRepository;
        this.channelRepository = channelRepository;
    }
@RequestMapping("/")
    public String home(Model model, OAuth2AuthenticationToken token) {
//        model.addAttribute("name", token.getPrincipal().getAttribute("name"));
//        model.addAttribute("email", token.getPrincipal().getAttribute("email"));
//        model.addAttribute("photo", token.getPrincipal().getAttribute("picture"));
    model.addAttribute("videoId", "pUTe9vsZ8do");
    model.addAttribute("categories",categoryRepository.findAll() );
    model.addAttribute("channels", channelRepository.findAll());
    model.addAttribute("category", new Category());

        return "index"; // Load Thymeleaf template
    }
    @PostMapping("/add-category")
    public String addCategory(@ModelAttribute Category category){
        categoryRepository.save(category);
        return "redirect:/";

    }
    @RequestMapping("/user")
    public Principal user(Principal user){
        return user;
    }

    @GetMapping("/subscriptions")
    public String getSubscriptions(OAuth2AuthenticationToken authentication, Model model) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            model.addAttribute("error", "Unauthorized: Please log in again.");
            return "subscriptions"; // Return to the Thymeleaf template with an error message
        }

        String accessToken = client.getAccessToken().getTokenValue();
     //   List<String> subscriptions = fetchSubscriptions(accessToken, null, new ArrayList<>());
        List<Channel> subscriptions = fetchSubscriptions(accessToken, null, new ArrayList<>());
       for(Channel channel : subscriptions){
           if(!channelRepository.findByYtId(channel.ytId).isPresent()){
               channelRepository.save(channel);
           }
       }

        model.addAttribute("subscriptions", subscriptions);
        return "subscriptions"; // This will render the "subscriptions.html" template
    }

    private List<Channel> fetchSubscriptions(String accessToken, String nextPageToken,  List<Channel> channels) {
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
                String ytId = item.path("id").asText();
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
    @GetMapping("/subs-managment")
    public String subManegment(Model model){
return"";
    }


}