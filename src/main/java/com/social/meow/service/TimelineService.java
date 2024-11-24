package com.social.meow.service;

import com.social.meow.model.Post;
import com.social.meow.repository.PostRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;

@Service
public class TimelineService {
        private final PostRepository postRepository;

        public TimelineService(PostRepository postRepository) {
            this.postRepository = postRepository;
        }

    @Cacheable(value = "posts", key = "#lastId != null ? #lastId.toString() : 'first'")
        public List<Post> getFirst20Posts() {
            PageRequest first20 = PageRequest.of(0, 20);
            return postRepository.findAll(first20).getContent();
        }

}
