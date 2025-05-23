package dev.greenn.youtubeviewer;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository extends MongoRepository<Channel, String> {

    List<Channel> findByCategoryIdsContaining(String categoryId);
    Optional<Channel> findByYtId(String ytId);
}