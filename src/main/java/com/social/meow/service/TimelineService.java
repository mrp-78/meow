package com.social.meow.service;

import com.social.meow.model.Post;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimelineService {
    private final PostService postService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${timeline.cache.key}")
    private String cacheKey;

    public TimelineService(PostService postService, RedisTemplate<String, Object> redisTemplate) {
        this.postService = postService;
        this.redisTemplate = redisTemplate;
    }

    public List<Long> getTimeline(Long lastId, int pageSize) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        List<Object> cachedIds = listOps.range(cacheKey, 0, -1);

        if (cachedIds == null || cachedIds.isEmpty()) {
            // Cache miss: load from DB
            List<Post> posts = postService.getPostsByLastId(lastId, pageSize);
//            return posts;
            return Collections.emptyList();
        }

        List<Long> ids = cachedIds.stream()
                .map(Object::toString)
                .map(Long::valueOf)
                .collect(Collectors.toList());

        int startIndex = 0;
        if (lastId != null) {
            int lastIndex = ids.indexOf(lastId);
            if (lastIndex == -1) {
                // Not found in cache → fallback to DB
                return Collections.emptyList();
//                return postService.getPostsByLastId(lastId, pageSize);
            }
            startIndex = lastIndex + 1;
        }

        int endIndex = Math.min(startIndex + pageSize, ids.size());
        List<Long> pageIds = ids.subList(startIndex, endIndex);
        return pageIds;

        // Fetch posts from DB by IDs (ensure order)
//        List<Post> posts = postRepository.findAllById(pageIds);

        // Preserve Redis order (descending by time)
//        Map<Long, Post> postMap = posts.stream()
//                .collect(Collectors.toMap(Post::getId, p -> p));
//        List<Post> ordered = pageIds.stream()
//                .map(postMap::get)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());

//        return ordered;
    }
}
