package com.social.meowkotlin.controller

import com.social.meowkotlin.model.Post
import com.social.meowkotlin.service.TimelineService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/timeline")
class TimelineController(timelineService: TimelineService) {
    private val timelineService: TimelineService = timelineService

    @GetMapping
    fun getTimeline(
        @RequestParam(value = "lastId", required = false) lastId: Long?
    ): ResponseEntity<List<Post>> {
        val timeline: List<Post> = timelineService.getTimeline(lastId, 3)
        return ResponseEntity.ok(timeline)
    }
}
