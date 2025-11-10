package com.instagram.media.service;

import com.instagram.common.exception.BadRequestException;
import com.instagram.media.dto.MediaUploadResponse;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/mpeg", "video/quicktime", "video/webm"
    );

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB

    public void initializeBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("Bucket created: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket", e);
            throw new RuntimeException("Failed to initialize storage", e);
        }
    }

    public MediaUploadResponse uploadImage(MultipartFile file, Long userId) {
        validateImageFile(file);

        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String fileName = generateFileName(userId, fileExtension);

            // Upload original image
            uploadToMinio(file.getInputStream(), fileName, file.getContentType(), file.getSize());

            // Generate and upload thumbnail
            String thumbnailFileName = generateThumbnailFileName(fileName);
            byte[] thumbnailData = generateThumbnail(file.getInputStream(), 300, 300);
            uploadToMinio(
                    new ByteArrayInputStream(thumbnailData),
                    thumbnailFileName,
                    "image/jpeg",
                    thumbnailData.length
            );

            String fileUrl = getFileUrl(fileName);
            String thumbnailUrl = getFileUrl(thumbnailFileName);

            log.info("Image uploaded: {} by user: {}", fileName, userId);

            return MediaUploadResponse.builder()
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .thumbnailUrl(thumbnailUrl)
                    .build();

        } catch (Exception e) {
            log.error("Error uploading image", e);
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    public MediaUploadResponse uploadVideo(MultipartFile file, Long userId) {
        validateVideoFile(file);

        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String fileName = generateFileName(userId, fileExtension);

            // Upload video
            uploadToMinio(file.getInputStream(), fileName, file.getContentType(), file.getSize());

            String fileUrl = getFileUrl(fileName);

            log.info("Video uploaded: {} by user: {}", fileName, userId);

            return MediaUploadResponse.builder()
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

        } catch (Exception e) {
            log.error("Error uploading video", e);
            throw new RuntimeException("Failed to upload video", e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("File deleted: {}", fileName);
        } catch (Exception e) {
            log.error("Error deleting file: {}", fileName, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public InputStream getFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving file: {}", fileName, e);
            throw new RuntimeException("Failed to retrieve file", e);
        }
    }

    private void uploadToMinio(InputStream inputStream, String fileName, String contentType, long size) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to MinIO", e);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only image files are allowed (JPEG, PNG, GIF, WebP)");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("Image size must not exceed 10MB");
        }
    }

    private void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        if (!ALLOWED_VIDEO_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only video files are allowed (MP4, MPEG, MOV, WebM)");
        }

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new BadRequestException("Video size must not exceed 100MB");
        }
    }

    private String generateFileName(Long userId, String extension) {
        return String.format("users/%d/%s.%s", userId, UUID.randomUUID(), extension);
    }

    private String generateThumbnailFileName(String originalFileName) {
        int lastDot = originalFileName.lastIndexOf('.');
        String nameWithoutExt = originalFileName.substring(0, lastDot);
        return nameWithoutExt + "_thumb.jpg";
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "jpg";
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "jpg";
    }

    private String getFileUrl(String fileName) {
        return String.format("%s/%s/%s", endpoint, bucketName, fileName);
    }

    private byte[] generateThumbnail(InputStream inputStream, int width, int height) throws Exception {
        BufferedImage originalImage = ImageIO.read(inputStream);

        if (originalImage == null) {
            throw new BadRequestException("Invalid image file");
        }

        // Calculate aspect ratio
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        double aspectRatio = (double) originalWidth / originalHeight;

        int targetWidth = width;
        int targetHeight = height;

        if (aspectRatio > 1) {
            targetHeight = (int) (width / aspectRatio);
        } else {
            targetWidth = (int) (height * aspectRatio);
        }

        // Create thumbnail
        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = thumbnail.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, "jpg", baos);
        return baos.toByteArray();
    }
}
