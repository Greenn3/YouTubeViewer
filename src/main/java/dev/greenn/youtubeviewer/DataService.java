package dev.greenn.youtubeviewer;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataService {

   private final ChannelRepository channelRepository;
   private final CategoryRepository categoryRepository;


    public DataService(ChannelRepository channelRepository, CategoryRepository categoryRepository) {
        this.channelRepository = channelRepository;
        this.categoryRepository = categoryRepository;
    }

    public void printData() {
        List<Channel> channels = channelRepository.findAll();
        List<Category> categories = categoryRepository.findAll();

        System.out.println("Channels:");
        channels.forEach(channel -> System.out.println(channel.getName()));

        System.out.println("\nCategories:");
     categories.forEach(category -> System.out.println(category.getName()));
    }
}
