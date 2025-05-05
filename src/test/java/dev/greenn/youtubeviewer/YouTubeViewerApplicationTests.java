package dev.greenn.youtubeviewer;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class YouTubeViewerApplicationTests {

    @Test
    void contextLoads() {
        assertThat(10).isEqualTo(5+5);
    }

}
