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

    public String saveImages(MultipartFile image, String uploadDir, String imageUrlCode) {
        String imageUrl = "";

        // Get the original filename
        String originalFilename = image.getOriginalFilename();

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
            // Validate the file (check if it is empty or not a valid image)
            if (image.isEmpty() || !isValidImage(image)) {
                throw new IllegalArgumentException("The file is not a valid image.");
            }

            // Save the image to the specified directory
            Files.copy(image.getInputStream(), targetPath);

            // Generate the URL for accessing the saved image
            imageUrl = imageUrlCode + fileName;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while saving the image.", e);
        }

        return imageUrl;
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
    private static boolean isValidImage(MultipartFile image) {
        // Get the content type (MIME type) of the uploaded file
        String contentType = image.getContentType();

        // Return true if the content type is not null and matches either JPEG or PNG
        // formats
        return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png"));
    }

}
