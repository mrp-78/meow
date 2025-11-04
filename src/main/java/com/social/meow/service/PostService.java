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
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Cache postsCache;

    @Value("${timeline.cache.key}")
    private String timelineCacheKey;

    @Value("${post.cache.key}")
    private String postCacheKey;

    @Value("${timeline.cache.maxSize}")
    private int maxTimelineSize;

    public PostService(PostRepository postRepository, RedisTemplate<String, Object> redisTemplate, CacheManager cacheManager) {
        this.postRepository = postRepository;
        this.redisTemplate = redisTemplate;
        this.postsCache = cacheManager.getCache(postCacheKey);
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

        if (postsCache != null) {
            postsCache.put(savedPost.getId(), savedPost);
        }
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

            if (postsCache != null) {
                postsCache.put(savedPost.getId(), savedPost);
            }
            return savedPost;
        }
        return null;
    }

    public void deletePost(long id) {
        postRepository.deleteById(id);

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

    public List<Post> getPostsByLastId(Long lastId, int pageSize) {
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by("id").descending());
        if (lastId == null) {
            return postRepository.findAll(pageable).getContent();
        }
        return postRepository.findByIdLessThanOrderByIdDesc(lastId, pageable);
    }
}
