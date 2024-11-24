package com.social.meow.controller;

import com.social.meow.model.Post;
import com.social.meow.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/timeline")
public class TimeLineController {

    @Autowired
    PostRepository postRepository;

    @GetMapping()
    public ResponseEntity<List<Post>> getAllPosts(
            @RequestParam(required = false) Long lastId) {
        try {
            List<Post> posts = new ArrayList<Post>();
            if(lastId != null) {
                Pageable next10 = PageRequest.of(0, 10);
                posts = postRepository.findByIdGreaterThanOrderByIdAsc(lastId, next10);
            }else{
                Pageable first10 = PageRequest.of(0,10);
                posts = postRepository.findAll(first10).getContent();
            }

            return new ResponseEntity<>(posts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
