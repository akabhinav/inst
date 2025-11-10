package com.instagram.media.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaUploadResponse {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private String thumbnailUrl;
}
