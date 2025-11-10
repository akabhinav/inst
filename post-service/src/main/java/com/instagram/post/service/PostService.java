package com.instagram.post.service;

import com.instagram.common.dto.PageResponse;
import com.instagram.common.exception.BadRequestException;
import com.instagram.common.exception.ResourceNotFoundException;
import com.instagram.post.dto.*;
import com.instagram.post.model.Comment;
import com.instagram.post.model.Like;
import com.instagram.post.model.Post;
import com.instagram.post.repository.CommentRepository;
import com.instagram.post.repository.LikeRepository;
import com.instagram.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request) {
        Post post = Post.builder()
                .userId(userId)
                .caption(request.getCaption())
                .mediaUrls(request.getMediaUrls())
                .mediaType(request.getMediaType())
                .location(request.getLocation())
                .build();

        post = postRepository.save(post);
        log.info("Post created: {} by user: {}", post.getId(), userId);

        // Publish event to Kafka for feed generation
        kafkaTemplate.send("post-events",
            String.format("POST_CREATED:%d:%d", post.getId(), userId));

        return mapToPostResponse(post, userId);
    }

    @Cacheable(value = "posts", key = "#postId")
    public PostResponse getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        return mapToPostResponse(post, currentUserId);
    }

    public PageResponse<PostResponse> getUserPosts(Long userId, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByUserIdAndIsArchivedFalseOrderByCreatedAtDesc(userId, pageable);

        List<PostResponse> postResponses = posts.getContent().stream()
                .map(post -> mapToPostResponse(post, currentUserId))
                .collect(Collectors.toList());

        return buildPageResponse(posts, postResponses, page, size);
    }

    public PageResponse<PostResponse> getExplorePosts(Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findAllActivePosts(pageable);

        List<PostResponse> postResponses = posts.getContent().stream()
                .map(post -> mapToPostResponse(post, currentUserId))
                .collect(Collectors.toList());

        return buildPageResponse(posts, postResponses, page, size);
    }

    @Transactional
    @CacheEvict(value = "posts", key = "#postId")
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (!post.getUserId().equals(userId)) {
            throw new BadRequestException("You can only delete your own posts");
        }

        // Soft delete
        post.setIsArchived(true);
        postRepository.save(post);

        log.info("Post deleted: {} by user: {}", postId, userId);

        // Publish event to Kafka
        kafkaTemplate.send("post-events",
            String.format("POST_DELETED:%d:%d", postId, userId));
    }

    @Transactional
    @CacheEvict(value = "posts", key = "#postId")
    public PostResponse likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // Check if already liked
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new BadRequestException("Post already liked");
        }

        // Create like
        Like like = Like.builder()
                .userId(userId)
                .postId(postId)
                .build();
        likeRepository.save(like);

        // Update post likes count
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);

        log.info("Post {} liked by user {}", postId, userId);

        // Publish event to Kafka for notification
        kafkaTemplate.send("post-events",
            String.format("POST_LIKED:%d:%d:%d", postId, userId, post.getUserId()));

        return mapToPostResponse(post, userId);
    }

    @Transactional
    @CacheEvict(value = "posts", key = "#postId")
    public PostResponse unlikePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // Check if liked
        if (!likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new BadRequestException("Post not liked");
        }

        // Delete like
        likeRepository.deleteByUserIdAndPostId(userId, postId);

        // Update post likes count
        post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
        postRepository.save(post);

        log.info("Post {} unliked by user {}", postId, userId);

        return mapToPostResponse(post, userId);
    }

    @Transactional
    public CommentResponse addComment(Long postId, Long userId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // Validate parent comment if provided
        if (request.getParentCommentId() != null) {
            if (!commentRepository.existsById(request.getParentCommentId())) {
                throw new ResourceNotFoundException("Comment", "id", request.getParentCommentId());
            }
        }

        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .parentCommentId(request.getParentCommentId())
                .content(request.getContent())
                .build();

        comment = commentRepository.save(comment);

        // Update post comments count (only for top-level comments)
        if (request.getParentCommentId() == null) {
            post.setCommentsCount(post.getCommentsCount() + 1);
            postRepository.save(post);
        }

        log.info("Comment added on post {} by user {}", postId, userId);

        // Publish event to Kafka for notification
        kafkaTemplate.send("post-events",
            String.format("COMMENT_ADDED:%d:%d:%d", postId, userId, post.getUserId()));

        return mapToCommentResponse(comment);
    }

    public PageResponse<CommentResponse> getPostComments(Long postId, int page, int size) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository
                .findByPostIdAndParentCommentIdIsNullOrderByCreatedAtDesc(postId, pageable);

        List<CommentResponse> commentResponses = comments.getContent().stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());

        return PageResponse.<CommentResponse>builder()
                .content(commentResponses)
                .page(page)
                .size(size)
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .hasNext(comments.hasNext())
                .hasPrevious(comments.hasPrevious())
                .build();
    }

    public PageResponse<CommentResponse> getCommentReplies(Long commentId, int page, int size) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> replies = commentRepository
                .findByParentCommentIdOrderByCreatedAtAsc(commentId, pageable);

        List<CommentResponse> replyResponses = replies.getContent().stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());

        return PageResponse.<CommentResponse>builder()
                .content(replyResponses)
                .page(page)
                .size(size)
                .totalElements(replies.getTotalElements())
                .totalPages(replies.getTotalPages())
                .hasNext(replies.hasNext())
                .hasPrevious(replies.hasPrevious())
                .build();
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getUserId().equals(userId)) {
            throw new BadRequestException("You can only delete your own comments");
        }

        commentRepository.delete(comment);

        // Update post comments count (only for top-level comments)
        if (comment.getParentCommentId() == null) {
            Post post = postRepository.findById(comment.getPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Post", "id", comment.getPostId()));
            post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
            postRepository.save(post);
        }

        log.info("Comment {} deleted by user {}", commentId, userId);
    }

    private PostResponse mapToPostResponse(Post post, Long currentUserId) {
        boolean isLiked = currentUserId != null &&
                likeRepository.existsByUserIdAndPostId(currentUserId, post.getId());

        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .caption(post.getCaption())
                .mediaUrls(post.getMediaUrls())
                .mediaType(post.getMediaType())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .location(post.getLocation())
                .isLiked(isLiked)
                .createdAt(post.getCreatedAt())
                .build();
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        long repliesCount = commentRepository.countByParentCommentId(comment.getId());

        return CommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .postId(comment.getPostId())
                .parentCommentId(comment.getParentCommentId())
                .content(comment.getContent())
                .likesCount(comment.getLikesCount())
                .repliesCount((int) repliesCount)
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private <T> PageResponse<T> buildPageResponse(Page<?> page, List<T> content, int pageNum, int size) {
        return PageResponse.<T>builder()
                .content(content)
                .page(pageNum)
                .size(size)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
