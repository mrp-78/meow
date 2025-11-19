package com.social.meowkotlin.repository

import com.social.meowkotlin.model.Post
import org.springframework.data.jpa.repository.JpaRepository

@org.springframework.stereotype.Repository
interface PostRepository : JpaRepository<Post, Long> {
    fun findByIdLessThanOrderByIdDesc(lastId: Long, pageable: org.springframework.data.domain.Pageable): List<Post>
}
