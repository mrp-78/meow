package com.social.meow.controller;

import com.social.meow.model.Post;
import com.social.meow.service.TimelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/timeline")
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping
    public ResponseEntity<List<Post>> getTimeline(
            @RequestParam(value = "lastId", required = false) Long lastId
    ) {
        List<Post> timeline = timelineService.getTimeline(lastId, 3);
        return ResponseEntity.ok(timeline);
    }
}
