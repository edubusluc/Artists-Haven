package com.artists_heaven.images;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class ImageServingUtil {
    ImageServingUtil() {
        // evitar instanciación
    }

    /**
     * Serves an image file from the specified path.
     *
     * @param basePath the base directory of the image
     * @param fileName the name of the image file
     * @return a ResponseEntity containing the image resource, or a 404 if not found
     */
    public ResponseEntity<Resource> serveImage(String basePath, String fileName) {
        Path filePath = Paths.get(basePath, fileName);
        Resource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .body(resource);
    }

    /**
     * Serves a video file from the specified path.
     *
     * @param basePath the base directory of the video
     * @param fileName the name of the video file
     * @return a ResponseEntity containing the video resource, or a 404 if not found
     */
    public ResponseEntity<Resource> serveVideo(String basePath, String fileName) {
        Path filePath = Paths.get(basePath, fileName);
        Resource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                .body(resource);
    }

    /**
     * Saves an uploaded image or video file to the server.
     *
     * @param file         the MultipartFile to save
     * @param uploadDir    the directory where the file should be saved
     * @param mediaUrlCode the URL prefix to access the saved file
     * @param allowVideo   whether to allow video files (mp4/quicktime)
     * @return the accessible media URL of the saved file
     * @throws IllegalArgumentException if the file is invalid or cannot be saved
     */
    public String saveImages(MultipartFile file, String uploadDir, String mediaUrlCode, boolean allowVideo) {
        String mediaUrl = "";

        // Get the original filename
        String originalFilename = file.getOriginalFilename();

        // Validate the filename
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("The file name is invalid.");
        }

        // Sanitize the filename to remove any invalid characters
        originalFilename = sanitizeFilename(originalFilename);

        // Generate a unique filename to prevent conflicts
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Path targetPath = Paths.get(uploadDir, fileName);

        try {
            if (file.isEmpty() || !isValidMedia(file, allowVideo)) {
                throw new IllegalArgumentException("The file is not a valid image or video.");
            }

            Files.copy(file.getInputStream(), targetPath);
            mediaUrl = mediaUrlCode + fileName;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while saving the file.", e);
        }

        return mediaUrl;
    }

    /**
     * Sanitizes the provided filename by replacing invalid characters with an
     * underscore.
     *
     * @param filename the original filename to sanitize.
     * @return a sanitized version of the filename, ensuring it only contains valid
     *         characters.
     */
    private String sanitizeFilename(String filename) {
        // Replace any character that is not a letter, number, dot, underscore, or
        // hyphen with an underscore
        return filename.replaceAll("[^a-zA-Z0-9\\._-]", "_");
    }

    /**
     * Checks if the provided file is a valid image.
     *
     * @param image the MultipartFile to validate.
     * @return true if the file is a JPEG or PNG image, false otherwise.
     */
    private static boolean isValidMedia(MultipartFile file, boolean allowVideo) {
        String contentType = file.getContentType();
        if (contentType == null)
            return false;

        if (contentType.equals("image/jpeg") || contentType.equals("image/png")) {
            return true;
        }

        if (allowVideo) {
            return contentType.equals("video/mp4") || contentType.equals("video/quicktime");
        }

        return false;
    }

}
