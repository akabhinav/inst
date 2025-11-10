package com.instagram.media.controller;

import com.instagram.common.dto.ApiResponse;
import com.instagram.media.dto.MediaUploadResponse;
import com.instagram.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload/image")
    public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        MediaUploadResponse response = mediaService.uploadImage(file, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Image uploaded successfully", response));
    }

    @PostMapping("/upload/video")
    public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        MediaUploadResponse response = mediaService.uploadVideo(file, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Video uploaded successfully", response));
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable String fileName) {
        InputStream inputStream = mediaService.getFile(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String fileName) {
        mediaService.deleteFile(fileName);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }
}
