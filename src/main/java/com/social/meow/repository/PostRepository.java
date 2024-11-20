package com.social.meow.repository;

import com.social.meow.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface  PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.id < :lastId ORDER BY p.id DESC")
    Page<Post> findPostsBeforeLastId(Long lastId, Pageable pageable);
}
