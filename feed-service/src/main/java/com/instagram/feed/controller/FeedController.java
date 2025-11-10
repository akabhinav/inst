package com.instagram.feed.controller;

import com.instagram.common.dto.ApiResponse;
import com.instagram.feed.dto.FeedPostResponse;
import com.instagram.feed.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<List<FeedPostResponse>>> getHomeFeed(
            @RequestParam("userId") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<FeedPostResponse> feed = feedService.getHomeFeed(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(feed));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<FeedPostResponse>>> getUserTimeline(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<FeedPostResponse> timeline = feedService.getUserTimeline(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(timeline));
    }
}
