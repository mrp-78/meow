package com.social.meowkotlin.service

import com.social.meowkotlin.model.Post
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.stream.Collectors
import kotlin.math.min

@Service
class TimelineService(private val postService: PostService, private val redisTemplate: RedisTemplate<String, Any>) {

    @Value("\${timeline.cache.key}")
    private val cacheKey: String? = null

    fun getTimeline(lastId: Long?, pageSize: Int): List<Post> {
        val listOps: ListOperations<String, Any> = redisTemplate.opsForList()
        val cachedIds: List<Any>? = cacheKey?.let { listOps.range(it, 0, -1) }

        if (cachedIds === null || cachedIds.isEmpty()) {
            val posts: List<Post> = postService.getPostsByLastIdAndPageSize(lastId, pageSize)
            return posts
        }

        val ids = cachedIds.stream()
            .map { obj: Any -> obj.toString() }
            .map<Long?> { s: String? -> s?.toLong() }
            .collect(Collectors.toList())

        var startIndex = 0
        if (lastId != null) {
            val lastIndex = ids.indexOf(lastId)
            if (lastIndex == -1) {
                return postService.getPostsByLastIdAndPageSize(lastId, pageSize)
            }
            startIndex = lastIndex + 1
        }

        val endIndex = min((startIndex + pageSize).toDouble(), ids.size.toDouble()).toInt()
        val pageIds = ids.subList(startIndex, endIndex)
        val posts: List<Post> = postService.getPostsByIds(pageIds)
        return posts
    }
}
