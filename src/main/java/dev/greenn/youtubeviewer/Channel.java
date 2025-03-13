package dev.greenn.youtubeviewer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Document(collection = "channels")
public class Channel {
    @Id
    String id;
    String name;
    String ytId;
    List<String> categoryIds = new ArrayList<>();


    public Channel(String name, String ytId) {
        this.name = name;
        this.ytId = ytId;
    }
}
