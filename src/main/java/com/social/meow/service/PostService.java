package com.social.meow.service;

import com.social.meow.model.Post;
import com.social.meow.repository.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManager cacheManager;

    @Value("${timeline.cache.key}")
    private String timelineCacheKey;

    @Value("${post.cache.key}")
    private String postCacheKey;

    @Value("${timeline.cache.maxSize}")
    private int maxTimelineSize;

    public PostService(PostRepository postRepository, RedisTemplate<String, Object> redisTemplate, CacheManager cacheManager) {
        this.postRepository = postRepository;
        this.redisTemplate = redisTemplate;
        this.cacheManager = cacheManager;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(long id) {
        Optional<Post> postData = postRepository.findById(id);
        return postData.orElse(null);
    }

    public Post createPost(Post post) {
        Post savedPost = postRepository.save(post);
        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                RedisOperations<String, Object> opsTemplate = (RedisOperations<String, Object>) operations;
                ListOperations<String, Object> listOps = opsTemplate.opsForList();

                listOps.leftPush(timelineCacheKey, savedPost.getId());
                listOps.trim(timelineCacheKey, 0, maxTimelineSize - 1);

                return null;
            }
        });

        return savedPost;
    }

    public Post updatePost(long id, Post post) {
        Optional<Post> postData = postRepository.findById(id);
        if (postData.isPresent()) {
            Post existingPost = postData.get();
            existingPost.setText(post.getText());
            existingPost.setUserId(post.getUserId());

            Post savedPost = postRepository.save(existingPost);
            Cache postsCache = cacheManager.getCache(postCacheKey);
            if (postsCache != null) {
                postsCache.evict(savedPost.getId());
            }
            return savedPost;
        }
        return null;
    }

    public void deletePost(long id) {
        postRepository.deleteById(id);
        Cache postsCache = cacheManager.getCache(postCacheKey);
        if (postsCache != null) {
            postsCache.evict(id);
        }
        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                RedisOperations<String, Object> opsTemplate = (RedisOperations<String, Object>) operations;
                ListOperations<String, Object> listOps = opsTemplate.opsForList();

                listOps.remove(timelineCacheKey, 0, id);

                return null;
            }
        });
    }

    public List<Post> getPostsByLastIdAndPageSize(Long lastId, int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by("id").descending());
        Cache postsCache = cacheManager.getCache(postCacheKey);
        List<Post> posts;
        if (lastId == null) {
            posts = postRepository.findAll(pageable).getContent();
        }
        else {
            posts = postRepository.findByIdLessThanOrderByIdDesc(lastId, pageable);
        }
        if (postsCache != null) {
            for (Post post : posts) {
                postsCache.put(post.getId(), post);
            }
        }
        return posts;
    }

    public List<Post> getPostsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        Cache postsCache = cacheManager.getCache(postCacheKey);
        Map<Long, Post> result = new LinkedHashMap<>();
        List<Long> missedIds = new ArrayList<>();

        for (Long id : ids) {
            Post cached = postsCache != null ? postsCache.get(id, Post.class) : null;
            if (cached != null) {
                result.put(id, cached);
            } else {
                missedIds.add(id);
            }
        }

        if (!missedIds.isEmpty()) {
            List<Post> dbPosts = postRepository.findAllById(missedIds);

            if (postsCache != null) {
                for (Post post : dbPosts) {
                    postsCache.put(post.getId(), post);
                    result.put(post.getId(), post);
                }
            } else {
                dbPosts.forEach(p -> result.put(p.getId(), p));
            }
        }

        return ids.stream()
                .map(result::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
