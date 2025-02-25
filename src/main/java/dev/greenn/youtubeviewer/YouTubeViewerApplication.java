package dev.greenn.youtubeviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class YouTubeViewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouTubeViewerApplication.class, args);
    }

}
