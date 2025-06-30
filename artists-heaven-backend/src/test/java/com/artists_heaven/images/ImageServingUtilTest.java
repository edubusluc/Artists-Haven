package com.artists_heaven.images;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ImageServingUtilTest {

    private ImageServingUtil imageServingUtil;
    @TempDir
    Path tempDir;

    private String basePath;

    @BeforeEach
    void setUp() {
        basePath = tempDir.toAbsolutePath().toString() + "/";
        imageServingUtil = new ImageServingUtil();
    }

    @Test
    void testServeImage_FileExists() throws IOException {
        // Arrange
        String fileName = "test.png";
        Path filePath = Paths.get(basePath, fileName);
        byte[] content = new byte[] { (byte) 137, 80, 78, 71 }; // PNG header
        Files.write(filePath, content);

        // Act
        ResponseEntity<Resource> response = imageServingUtil.serveImage(basePath, fileName);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("image/png", Objects.requireNonNull(response.getHeaders().getContentType()).toString());
    }

    @Test
    void testServeImage_FileDoesNotExist() {
        // Act
        ResponseEntity<Resource> response = imageServingUtil.serveImage(basePath, "nonexistent.png");

        // Assert
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void testSaveImages_Success() throws IOException {
        // Arrange
        byte[] content = new byte[] { (byte) 137, 80, 78, 71 };
        MockMultipartFile multipartFile = new MockMultipartFile(
                "image", "photo.png", "image/png", content);

        // Act
        String imageUrl = imageServingUtil.saveImages(multipartFile, basePath, "/imageUrlCode/",false);

        // Assert
        assertNotNull(imageUrl);
        assertTrue(imageUrl.endsWith(".png"));
        Path savedFile = Paths.get(basePath, imageUrl.substring(imageUrl.lastIndexOf('/') + 1));
        assertTrue(Files.exists(savedFile));
    }

    @Test
    void testSaveImages_InvalidMimeType() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "image", "bad.gif", "image/gif", "GIF89a".getBytes());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageServingUtil.saveImages(file, basePath, "/imageUrlCode/",false));

        assertTrue(ex.getMessage().contains("not a valid image"));
    }

    @Test
    void testSaveImages_EmptyFile() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "image", "empty.png", "image/png", new byte[0]);

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageServingUtil.saveImages(file, basePath, "/imageUrlCode/",false));

        assertTrue(ex.getMessage().contains("not a valid image"));
    }

    @Test
    void testSaveImages_InvalidFilename() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "image", "", "image/png", new byte[] { (byte) 137, 80, 78, 71 });

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> imageServingUtil.saveImages(file, basePath, "/imageUrlCode/",false));

        assertTrue(ex.getMessage().contains("file name is invalid"));
    }
}
