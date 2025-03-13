package dev.greenn.youtubeviewer;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;


public class DatabaseSeeder implements CommandLineRunner {
    private final ChannelRepository channelRepository;
    private final CategoryRepository categoryRepository;

    public DatabaseSeeder(ChannelRepository channelRepository, CategoryRepository categoryRepository) {
        this.channelRepository = channelRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Creating categories
        Category techCategory = new Category(null, "Technology", List.of());
        Category musicCategory = new Category(null, "Music", List.of());

        techCategory = categoryRepository.save(techCategory);
        musicCategory = categoryRepository.save(musicCategory);

        // Creating channels
        Channel ytChannel1 = new Channel(null, "TechWithTim", "UC4JX40jDee_tINbkjycV4Sg", List.of(techCategory.getId()));
        Channel ytChannel2 = new Channel(null, "Lo-Fi Beats", "UCx2SxOvxE0D2lA6wFHDmJzA", List.of(musicCategory.getId()));

        channelRepository.save(ytChannel1);
        channelRepository.save(ytChannel2);

        System.out.println("✅ Sample data inserted!");
    }
}
