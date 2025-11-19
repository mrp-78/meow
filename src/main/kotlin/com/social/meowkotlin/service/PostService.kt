package com.social.meowkotlin.service

import com.social.meowkotlin.model.Post
import com.social.meowkotlin.repository.PostRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.dao.DataAccessException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SessionCallback
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

@Service
class PostService(
    private val postRepository: PostRepository,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val cacheManager: CacheManager
) {

    @Value("\${timeline.cache.key}")
    private val timelineCacheKey: String? = null

    @Value("\${post.cache.key}")
    private val postCacheKey: String? = null

    @Value("\${timeline.cache.maxSize}")
    private val maxTimelineSize = 0

    fun getPostById(id: Long): Post? {
        val postData: Optional<Post?> = postRepository.findById(id)
        return postData.orElse(null)
    }

    fun createPost(post: Post): Post {
        val savedPost: Post = postRepository.save(post)
        redisTemplate.executePipelined(object : SessionCallback<Any?> {
            @Throws(DataAccessException::class)
            override fun <K, V> execute(operations: RedisOperations<K, V>): Any? {
                val opsTemplate: RedisOperations<String, Any> = operations as RedisOperations<String, Any>
                val listOps: ListOperations<String, Any> = opsTemplate.opsForList()

                if (timelineCacheKey !== null) {
                    listOps.leftPush(timelineCacheKey, savedPost.id)
                    listOps.trim(timelineCacheKey, 0, (maxTimelineSize - 1).toLong())
                }

                return null
            }
        })

        return savedPost
    }

    fun updatePost(id: Long, post: Post): Post? {
        val postData: Optional<Post?> = postRepository.findById(id)
        if (postData.isPresent()) {
            val existingPost: Post = postData.get()
            existingPost.text = post.text
            existingPost.userId = post.userId

            val savedPost: Post = postRepository.save(existingPost)
            if (postCacheKey !== null) {
                val postsCache: Cache? = cacheManager.getCache(postCacheKey)
                postsCache?.evict(savedPost.id)
            }
            return savedPost
        }
        return null
    }

    fun deletePost(id: Long) {
        postRepository.deleteById(id)
        if (postCacheKey !== null) {
            val postsCache: Cache? = cacheManager.getCache(postCacheKey)
            postsCache?.evict(id)
        }
        redisTemplate.executePipelined(object : SessionCallback<Any?> {
            @Throws(DataAccessException::class)
            override fun <K, V> execute(operations: RedisOperations<K, V>): Any? {
                val opsTemplate: RedisOperations<String, Any> = operations as RedisOperations<String, Any>
                val listOps: ListOperations<String, Any> = opsTemplate.opsForList()

                if (timelineCacheKey !== null) listOps.remove(timelineCacheKey, 0, id)

                return null
            }
        })
    }

    fun getPostsByLastIdAndPageSize(lastId: Long?, pageSize: Int): List<Post> {
        val pageable: Pageable = PageRequest.of(0, pageSize, Sort.by("id").descending())
        val posts: List<Post> = if (lastId == null) {
            postRepository.findAll(pageable).getContent()
        } else {
            postRepository.findByIdLessThanOrderByIdDesc(lastId, pageable)
        }
        if (postCacheKey !== null) {
            val postsCache: Cache? = cacheManager.getCache(postCacheKey)
            if (postsCache != null) {
                for (post in posts) {
                    postsCache.put(post.id, post)
                }
            }
        }
        return posts
    }

    fun getPostsByIds(ids: List<Long>): List<Post> {
        if (ids == null || ids.isEmpty()) {
            return emptyList<Post>()
        }

        val postsCache: Cache? = postCacheKey?.let { cacheManager.getCache(it) }
        val result: MutableMap<Long, Post> = LinkedHashMap<Long, Post>()
        val missedIds: MutableList<Long?> = ArrayList()

        for (id in ids) {
            val cached: Post? = if (postsCache != null) postsCache.get(id, Post::class.java) else null
            if (cached != null) {
                result[id] = cached
            } else {
                missedIds.add(id)
            }
        }

        if (missedIds.isNotEmpty()) {
            val dbPosts: List<Post> = postRepository.findAllById(missedIds)

            if (postsCache != null) {
                for (post in dbPosts) {
                    postsCache.put(post.id, post)
                    result[post.id] = post
                }
            } else {
                dbPosts.forEach(Consumer { p: Post -> result[p.id] = p })
            }
        }

        return ids.mapNotNull { result[it] }
    }
}
