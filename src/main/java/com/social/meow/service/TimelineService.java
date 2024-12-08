package com.social.meow.service;

import com.social.meow.model.Post;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimelineService {
    private final PostService postService;

    public TimelineService(PostService postService) {
        this.postService = postService;
    }

    @Cacheable(value = "timelineCache", key = "#lastId != null ? #lastId.toString() : 'first'")
    public List<Post> getTimeline(Long lastId, int pageSize) {
        return postService.getPostsByLastId(lastId, pageSize);
    }
}
