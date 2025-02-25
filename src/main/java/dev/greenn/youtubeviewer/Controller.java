package dev.greenn.youtubeviewer;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.stereotype.Controller
public class Controller {
    @GetMapping("/video")
    public String showVideo(Model model) {
        String videoId = "c8iZksB-d0I"; // ID wideo YouTube
        model.addAttribute("videoId", videoId);
        return "index"; // Ładuje szablon `index.html`
    }
}
