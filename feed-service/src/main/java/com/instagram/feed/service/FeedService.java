package com.instagram.feed.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instagram.feed.dto.FeedPostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String FEED_KEY_PREFIX = "feed:";
    private static final String USER_TIMELINE_KEY_PREFIX = "timeline:";
    private static final int FEED_CACHE_TTL = 300; // 5 minutes
    private static final int MAX_FEED_SIZE = 500;

    /**
     * Get home feed for a user (posts from users they follow)
     */
    public List<FeedPostResponse> getHomeFeed(Long userId, int page, int size) {
        String feedKey = FEED_KEY_PREFIX + userId;

        // Try to get from cache first
        Set<String> cachedPosts = redisTemplate.opsForZSet()
                .reverseRange(feedKey, page * size, (page + 1) * size - 1);

        if (cachedPosts != null && !cachedPosts.isEmpty()) {
            log.info("Feed cache hit for user: {}", userId);
            return cachedPosts.stream()
                    .map(this::deserializePost)
                    .collect(Collectors.toList());
        }

        // Cache miss - generate feed
        log.info("Feed cache miss for user: {}, generating...", userId);
        return generateFeed(userId, page, size);
    }

    /**
     * Get user's own posts timeline
     */
    public List<FeedPostResponse> getUserTimeline(Long userId, int page, int size) {
        String timelineKey = USER_TIMELINE_KEY_PREFIX + userId;

        Set<String> cachedPosts = redisTemplate.opsForZSet()
                .reverseRange(timelineKey, page * size, (page + 1) * size - 1);

        if (cachedPosts != null && !cachedPosts.isEmpty()) {
            return cachedPosts.stream()
                    .map(this::deserializePost)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    /**
     * Kafka listener for post creation events
     */
    @KafkaListener(topics = "post-events", groupId = "feed-service-group")
    public void handlePostEvent(String message) {
        log.info("Received post event: {}", message);

        try {
            String[] parts = message.split(":");
            String eventType = parts[0];

            if ("POST_CREATED".equals(eventType)) {
                Long postId = Long.parseLong(parts[1]);
                Long userId = Long.parseLong(parts[2]);
                handlePostCreated(postId, userId);
            } else if ("POST_DELETED".equals(eventType)) {
                Long postId = Long.parseLong(parts[1]);
                handlePostDeleted(postId);
            }
        } catch (Exception e) {
            log.error("Error processing post event: {}", message, e);
        }
    }

    /**
     * Kafka listener for user follow events
     */
    @KafkaListener(topics = "user-events", groupId = "feed-service-group")
    public void handleUserEvent(String message) {
        log.info("Received user event: {}", message);

        try {
            String[] parts = message.split(":");
            String eventType = parts[0];

            if ("USER_FOLLOWED".equals(eventType)) {
                Long followerId = Long.parseLong(parts[1]);
                Long followingId = Long.parseLong(parts[2]);
                handleUserFollowed(followerId, followingId);
            }
        } catch (Exception e) {
            log.error("Error processing user event: {}", message, e);
        }
    }

    /**
     * Handle new post creation - Fan-out on write
     */
    private void handlePostCreated(Long postId, Long userId) {
        try {
            // Get post details from post-service
            FeedPostResponse post = fetchPostDetails(postId);
            if (post == null) {
                return;
            }

            String postJson = serializePost(post);
            double score = System.currentTimeMillis();

            // Add to user's own timeline
            String userTimelineKey = USER_TIMELINE_KEY_PREFIX + userId;
            redisTemplate.opsForZSet().add(userTimelineKey, postJson, score);
            trimFeed(userTimelineKey);

            // Get followers from user-service
            List<Long> followerIds = fetchUserFollowers(userId);

            // Fan-out to followers' feeds (for non-celebrity users)
            if (followerIds.size() <= 1000) {
                for (Long followerId : followerIds) {
                    String feedKey = FEED_KEY_PREFIX + followerId;
                    redisTemplate.opsForZSet().add(feedKey, postJson, score);
                    trimFeed(feedKey);
                    redisTemplate.expire(feedKey, FEED_CACHE_TTL, TimeUnit.SECONDS);
                }
                log.info("Fanned out post {} to {} followers", postId, followerIds.size());
            }

        } catch (Exception e) {
            log.error("Error handling post creation: {}", postId, e);
        }
    }

    /**
     * Handle post deletion
     */
    private void handlePostDeleted(Long postId) {
        // In a real implementation, we would remove the post from all feeds
        // For simplicity, we'll let cache TTL handle it
        log.info("Post {} deleted, will be removed from feeds on cache expiry", postId);
    }

    /**
     * Handle new follow - populate feed with followed user's recent posts
     */
    private void handleUserFollowed(Long followerId, Long followingId) {
        try {
            String feedKey = FEED_KEY_PREFIX + followerId;

            // Fetch recent posts from the followed user
            List<FeedPostResponse> recentPosts = fetchUserRecentPosts(followingId, 20);

            // Add to follower's feed
            for (FeedPostResponse post : recentPosts) {
                String postJson = serializePost(post);
                double score = post.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC);
                redisTemplate.opsForZSet().add(feedKey, postJson, score);
            }

            trimFeed(feedKey);
            redisTemplate.expire(feedKey, FEED_CACHE_TTL, TimeUnit.SECONDS);

            log.info("Updated feed for user {} after following user {}", followerId, followingId);
        } catch (Exception e) {
            log.error("Error handling user follow event", e);
        }
    }

    /**
     * Generate feed on cache miss (Fan-out on read)
     */
    private List<FeedPostResponse> generateFeed(Long userId, int page, int size) {
        try {
            // Get list of users the current user follows
            List<Long> followingIds = fetchUserFollowing(userId);

            if (followingIds.isEmpty()) {
                return new ArrayList<>();
            }

            // Fetch recent posts from followed users
            List<FeedPostResponse> feed = new ArrayList<>();
            for (Long followingId : followingIds) {
                List<FeedPostResponse> posts = fetchUserRecentPosts(followingId, 10);
                feed.addAll(posts);
            }

            // Sort by creation time (descending)
            feed.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            // Cache the feed
            String feedKey = FEED_KEY_PREFIX + userId;
            for (FeedPostResponse post : feed) {
                String postJson = serializePost(post);
                double score = post.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC);
                redisTemplate.opsForZSet().add(feedKey, postJson, score);
            }
            redisTemplate.expire(feedKey, FEED_CACHE_TTL, TimeUnit.SECONDS);

            // Return paginated result
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, feed.size());
            return feed.subList(fromIndex, Math.min(toIndex, feed.size()));

        } catch (Exception e) {
            log.error("Error generating feed for user: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Trim feed to maximum size
     */
    private void trimFeed(String feedKey) {
        Long size = redisTemplate.opsForZSet().size(feedKey);
        if (size != null && size > MAX_FEED_SIZE) {
            redisTemplate.opsForZSet().removeRange(feedKey, 0, size - MAX_FEED_SIZE - 1);
        }
    }

    /**
     * Fetch post details from post-service
     */
    private FeedPostResponse fetchPostDetails(Long postId) {
        try {
            String url = "http://localhost:8082/api/posts/" + postId;
            return restTemplate.getForObject(url, FeedPostResponse.class);
        } catch (Exception e) {
            log.error("Error fetching post details for: {}", postId, e);
            return null;
        }
    }

    /**
     * Fetch user's followers from user-service
     */
    private List<Long> fetchUserFollowers(Long userId) {
        try {
            String url = "http://localhost:8081/api/users/" + userId + "/followers?size=10000";
            // Simplified - in real implementation, parse the response properly
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching followers for user: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Fetch users that current user is following
     */
    private List<Long> fetchUserFollowing(Long userId) {
        try {
            String url = "http://localhost:8081/api/users/" + userId + "/following?size=10000";
            // Simplified - in real implementation, parse the response properly
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching following for user: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Fetch recent posts from a user
     */
    private List<FeedPostResponse> fetchUserRecentPosts(Long userId, int limit) {
        try {
            String url = "http://localhost:8082/api/posts/user/" + userId + "?size=" + limit;
            // Simplified - in real implementation, parse the response properly
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching posts for user: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Serialize post to JSON
     */
    private String serializePost(FeedPostResponse post) {
        try {
            return objectMapper.writeValueAsString(post);
        } catch (JsonProcessingException e) {
            log.error("Error serializing post", e);
            return "";
        }
    }

    /**
     * Deserialize post from JSON
     */
    private FeedPostResponse deserializePost(String json) {
        try {
            return objectMapper.readValue(json, FeedPostResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing post", e);
            return null;
        }
    }
}
