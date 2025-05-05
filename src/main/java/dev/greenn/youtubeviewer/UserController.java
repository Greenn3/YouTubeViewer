package dev.greenn.youtubeviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class UserController {


    private final OAuth2AuthorizedClientService authorizedClientService;

    private final  DataService dataService;
    private final CategoryRepository categoryRepository;
    private final ChannelRepository channelRepository;
    private final ApiService apiService;
private  final YouTubeDataFetcher dataFetcher;
  
    @Value("${api.key}")
    private String API_KEY;
@Autowired
    public UserController(OAuth2AuthorizedClientService authorizedClientService, ApiService apiService, DataService dataService, CategoryRepository categoryRepository, ChannelRepository channelRepository, YouTubeDataFetcher dataFetcher) {
        this.authorizedClientService = authorizedClientService;
this.apiService = apiService;
        this.dataService = dataService;
        this.categoryRepository = categoryRepository;
        this.channelRepository = channelRepository;
    this.dataFetcher = dataFetcher;
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
        List<Channel> subscriptions =dataFetcher.fetchSubscriptions(accessToken, null, new ArrayList<>());
       for(Channel channel : subscriptions){
           if(!channelRepository.findByYtId(channel.ytId).isPresent()){
               channelRepository.save(channel);
           }
       }

        model.addAttribute("subscriptions", subscriptions);
        return "subscriptions"; // This will render the "subscriptions.html" template
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
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("channels", channelRepository.findAll());

return"/subs-managment";
    }

    @PostMapping("/assign-channels")
    public String assignChannels(
            @RequestParam ("categoryId") String categoryId,
            @RequestParam ("channelIds") List<String> channelIds
    ){
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category != null) {
            // Ensure channelIds list is initialized
            if (category.getChannelIds() == null) {
                category.setChannelIds(new ArrayList<>());
            }

            // Add only new channels (avoid duplicates)
            for (String channelId : channelIds) {
                if (!category.getChannelIds().contains(channelId)) {
                    category.getChannelIds().add(channelId);
                }
            }

            // Save updated category to MongoDB
            categoryRepository.save(category);
        }

        return "redirect:subs-managment";
    }

    @RequestMapping("/pick")
    public String pickCategory(Model model) {
model.addAttribute("categories", categoryRepository.findAll());
        // If no category is selected, show the page without a video


        return "pick";
    }

    @RequestMapping("/watch")
    public String watch(@RequestParam String categoryId, Model model, @RequestParam String days){

        Optional<Category> category = categoryRepository.findById(categoryId);
        System.out.println(categoryId);


        List<String> ytIds = category.get().getChannelIds().stream()
                .map(channelId -> channelRepository.findById(channelId)) // Fetch the Channel object
                .filter(Optional::isPresent) // Ensure it's present
                .map(optionalChannel -> optionalChannel.get().getYtId()) // Extract ytId
                .collect(Collectors.toList());



List<String> videoIds = new ArrayList<>();
        ytIds.forEach(System.out::println);
for(String id : ytIds){
// videoIds.add(apiService.getLatestVideo(id, API_KEY));
   for(String vidId : dataFetcher.getVideosAddedAfter(id, API_KEY, days)){
       videoIds.add(vidId);
    }
}
//apiService.getLatestVideoIds(ytIds, API_KEY);

model.addAttribute("videos", videoIds);
return "watch";
    }

    @RequestMapping("/yt-viewer-index")
    public String goToMain(Model model){
        model.addAttribute("videoId", "pUTe9vsZ8do");
        model.addAttribute("categories",categoryRepository.findAll() );
        model.addAttribute("channels", channelRepository.findAll());
        model.addAttribute("category", new Category());

        return "yt-viewer-index";
    }

    @RequestMapping("/exp-zone")
    public String experiment(Model model){
        model.addAttribute("videoId", "pUTe9vsZ8do");
        model.addAttribute("categories",categoryRepository.findAll() );
        model.addAttribute("channels", channelRepository.findAll());
        model.addAttribute("category", new Category());

        return "exp-zone";
    }

//    @RequestMapping("/api-test")
//    public String testApi(Model model) {
//
//
//
//        List<String> list = dataFetcher.getVideos(API_KEY).block();
//        model.addAttribute("list" , list);
//        return "/api-test";
//    }
//


}