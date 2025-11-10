package com.instagram.post.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    @Size(max = 2200, message = "Caption must not exceed 2200 characters")
    private String caption;

    @NotNull(message = "Media URLs are required")
    @Size(min = 1, max = 10, message = "Must have between 1 and 10 media items")
    private String[] mediaUrls;

    @NotNull(message = "Media type is required")
    private String mediaType; // IMAGE, VIDEO, CAROUSEL

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;
}
