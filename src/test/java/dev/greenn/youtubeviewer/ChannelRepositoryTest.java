package dev.greenn.youtubeviewer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableMongoRepositories(basePackages = "dev.greenn.youtubeviewer")  // Explicitly enabling repository scan
class ChannelRepositoryTest {

    @Autowired
    private ChannelRepository underTest;

    @Test
    void getById(){
        String ytId = "UC7XgxJhy6N6KGMFzELHtlUQ";
       Optional<Channel> optionalChannel = underTest.findByYtId(ytId);
       Channel channel = optionalChannel.get();
       String name = channel.getName();
       assertThat(name).isEqualTo("Urbex History");

    }

}