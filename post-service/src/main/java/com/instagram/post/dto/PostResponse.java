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
public class PostResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userProfileImage;
    private String caption;
    private String[] mediaUrls;
    private String mediaType;
    private Integer likesCount;
    private Integer commentsCount;
    private String location;
    private Boolean isLiked; // By current user
    private LocalDateTime createdAt;
}
