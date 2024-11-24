package com.social.meow.service;

import com.social.meow.model.Post;
import com.social.meow.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.cache.Cache;


import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CacheManager cacheManager;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(long id) {
        Cache cache = cacheManager.getCache("posts");
        if (cache != null && cache.get(id) != null) {
            return (Post) cache.get(id).get();
        }

        Optional<Post> postData = postRepository.findById(id);
        if (postData.isPresent()) {
            cache.put(id, postData.get());
            return postData.get();
        }
        return null;
    }

    public Post createPost(Post post) {
        Post savedPost = postRepository.save(post);
        // Write-through caching: update cache after saving to the DB
        Cache cache = cacheManager.getCache("posts");
        if (cache != null) {
            cache.put(savedPost.getId(), savedPost);
        }
        return savedPost;
    }

    public Post updatePost(long id, Post post) {
        Optional<Post> postData = postRepository.findById(id);
        if (postData.isPresent()) {
            Post existingPost = postData.get();
            existingPost.setText(post.getText());
            existingPost.setUserId(post.getUserId());
            Post updatedPost = postRepository.save(existingPost);

            Cache cache = cacheManager.getCache("posts");
            if (cache != null) {
                cache.put(updatedPost.getId(), updatedPost);
            }

            return updatedPost;
        }
        return null;
    }

    public void deletePost(long id) {
        postRepository.deleteById(id);
        // Invalidate the cache for the deleted post
        Cache cache = cacheManager.getCache("posts");
        if (cache != null) {
            cache.evict(id);
        }
    }
}
