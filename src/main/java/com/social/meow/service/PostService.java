package com.social.meow.service;

import com.social.meow.model.Post;
import com.social.meow.repository.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${timeline.cache.key}")
    private String cacheKey;

    @Value("${timeline.cache.maxSize}")
    private int maxTimelineSize;

    @Value("${timeline.cache.ttlSeconds}")
    private long ttlSeconds;

    public PostService(PostRepository postRepository, RedisTemplate<String, Object> redisTemplate) {
        this.postRepository = postRepository;
        this.redisTemplate = redisTemplate;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(long id) {
        Optional<Post> postData = postRepository.findById(id);
        return postData.orElse(null);
    }

    public Post createPost(Post post) {
        Post newPost = postRepository.save(post);

        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                RedisOperations<String, Object> opsTemplate = (RedisOperations<String, Object>) operations;
                ListOperations<String, Object> listOps = opsTemplate.opsForList();

                listOps.leftPush(cacheKey, Long.toString(newPost.getId()));
                listOps.trim(cacheKey, 0, maxTimelineSize - 1);
                opsTemplate.expire(cacheKey, Duration.ofSeconds(ttlSeconds));

                return null;
            }
        });

        return newPost;
    }

    public Post updatePost(long id, Post post) {
        Optional<Post> postData = postRepository.findById(id);
        if (postData.isPresent()) {
            Post existingPost = postData.get();
            existingPost.setText(post.getText());
            existingPost.setUserId(post.getUserId());

            return postRepository.save(existingPost);
        }
        return null;
    }

    public void deletePost(long id) {
        postRepository.deleteById(id);

        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                RedisOperations<String, Object> opsTemplate = (RedisOperations<String, Object>) operations;
                ListOperations<String, Object> listOps = opsTemplate.opsForList();

                listOps.remove(cacheKey, 0, Long.toString(id));
                opsTemplate.expire(cacheKey, Duration.ofSeconds(ttlSeconds));

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
