package com.social.meowkotlin.controller

import com.social.meowkotlin.model.Post
import com.social.meowkotlin.service.PostService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/posts")
class PostController(postService: PostService) {
    private val postService: PostService = postService

    @GetMapping("/{id}")
    fun getPostById(@PathVariable("id") id: Long): ResponseEntity<Post> {
        try {
            val post: Post? = postService.getPostById(id)

            return if (post != null) {
                ResponseEntity<Post>(post, HttpStatus.OK)
            } else {
                ResponseEntity<Post>(HttpStatus.NOT_FOUND)
            }
        } catch (e: Exception) {
            return ResponseEntity<Post>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping
    fun createPost(@RequestBody post: Post?): ResponseEntity<Post> {
        try {
            if (post != null) {
                val savedPost: Post = postService.createPost(post)
                return ResponseEntity<Post>(savedPost, HttpStatus.CREATED)
            }
            return ResponseEntity<Post>(HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (e: Exception) {
            System.out.printf("Error: %s", e)
            return ResponseEntity<Post>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PutMapping("/{id}")
    fun updatePost(@PathVariable("id") id: Long, @RequestBody post: Post?): ResponseEntity<Post> {
        try {
            if (post != null) {
                val updatedPost: Post? = postService.updatePost(id, post)
                return ResponseEntity<Post>(updatedPost, HttpStatus.CREATED)
            }
            return ResponseEntity<Post>(HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (e: Exception) {
            return ResponseEntity<Post>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteTutorial(@PathVariable("id") id: Long): ResponseEntity<HttpStatus> {
        try {
            postService.deletePost(id)
            return ResponseEntity<HttpStatus>(HttpStatus.NO_CONTENT)
        } catch (e: Exception) {
            return ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PostController::class.java)
    }
}