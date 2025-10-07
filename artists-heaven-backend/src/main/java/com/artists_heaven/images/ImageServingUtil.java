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
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
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
    public String saveMediaFile(MultipartFile file, String folderName, String publicUrlPrefix, boolean allowVideo) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("The file is empty or invalid.");
        }

        // Obtener y sanear el nombre original del archivo
        String originalFilename = sanitizeFilename(file.getOriginalFilename());

        // Obtener extensión
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        // Validar extensión
        List<String> allowedImageExtensions = Arrays.asList(".png", ".jpg", ".jpeg", ".webp");
        List<String> allowedVideoExtensions = Arrays.asList(".mp4", ".mov", ".avi", ".mkv");

        if (allowVideo) {
            if (!allowedImageExtensions.contains(extension) && !allowedVideoExtensions.contains(extension)) {
                throw new IllegalArgumentException("Invalid file type. Allowed image or video types.");
            }
        } else {
            if (!allowedImageExtensions.contains(extension)) {
                throw new IllegalArgumentException("Invalid file type. Only image types are allowed.");
            }
        }

        // Generar nombre único
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        // Carpeta absoluta dentro del proyecto
        Path targetPath = Paths.get(System.getProperty("user.dir"), folderName, uniqueFileName).normalize();

        try {
            // Crear carpeta si no existe
            Files.createDirectories(targetPath.getParent());

            // Guardar el archivo
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Devolver la URL pública para frontend
            return publicUrlPrefix + uniqueFileName;

        } catch (IOException e) {
            throw new IllegalArgumentException("Error while saving the file: " + e.getMessage(), e);
        }
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


}
