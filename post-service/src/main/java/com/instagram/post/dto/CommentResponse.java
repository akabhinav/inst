package com.instagram.post.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userProfileImage;
    private Long postId;
    private Long parentCommentId;
    private String content;
    private Integer likesCount;
    private Integer repliesCount;
    private LocalDateTime createdAt;
}
