package com.social.meow.repository;

import com.social.meow.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  PostRepository extends JpaRepository<Post, Long> { }
