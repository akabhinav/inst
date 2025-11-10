package com.instagram.user.dto;

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
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String profileImageUrl;
    private Integer followersCount;
    private Integer followingCount;
    private Integer postsCount;
    private Boolean isVerified;
    private Boolean isPrivate;
    private Boolean isFollowing; // For current user context
    private Boolean isFollowedBy; // For current user context
    private LocalDateTime createdAt;
}
