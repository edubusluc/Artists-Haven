package com.artists_heaven.images;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import java.nio.file.Path;
import java.nio.file.Paths;
public class ImageServingUtil {
    private ImageServingUtil() {
        // evitar instanciación
    }

    public static ResponseEntity<Resource> serveImage(String basePath, String fileName) {
        Path filePath = Paths.get(basePath, fileName);
        Resource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/png")
                .body(resource);
    }
    
}
