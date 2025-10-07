package com.artists_heaven.images;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

                // Asegúrate de que basePath esté bien definido
                String basePath = Paths.get(System.getProperty("user.dir"), "product_media").toString();

                // Act
                String imageUrl = imageServingUtil.saveMediaFile(multipartFile, "product_media", "/imageUrlCode/",
                                false);

                // Assert
                assertNotNull(imageUrl);
                assertTrue(imageUrl.endsWith(".png"));

                String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                Path savedFile = Paths.get(basePath, fileName);

                System.out.println("Expected saved file path: " + savedFile);

                assertTrue(Files.exists(savedFile));
                Files.deleteIfExists(savedFile);

        }

        @Test
        void testSaveImages_InvalidMimeType() {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                                "image", "bad.gif", "image/gif", "GIF89a".getBytes());

                // Act + Assert
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                () -> imageServingUtil.saveMediaFile(file, basePath, "/imageUrlCode/", false));

                assertTrue(ex.getMessage().contains("Only image types are allowed"));
        }

        @Test
        void testSaveImages_EmptyFile() {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                                "image", "empty.png", "image/png", new byte[0]);

                // Act + Assert
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                () -> imageServingUtil.saveMediaFile(file, basePath, "/imageUrlCode/", false));

                assertTrue(ex.getMessage().contains("The file is empty or invalid."));

        }

        @Test
        void testSaveImages_InvalidFilename() {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                                "image", "", "image/png", new byte[] { (byte) 137, 80, 78, 71 });

                // Act + Assert
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                () -> imageServingUtil.saveMediaFile(file, basePath, "/imageUrlCode/", false));

                assertTrue(ex.getMessage().contains("Invalid file type. Only image types are allowed."));
        }

        @Test
        void testSaveImages_ValidVideo_Allowed() throws IOException {
                // Arrange
                byte[] content = new byte[] { 0, 1, 2, 3 };
                MockMultipartFile file = new MockMultipartFile(
                                "video", "clip.mp4", "video/mp4", content);

                // Carpeta dentro del proyecto donde se guarda el archivo
                String folderName = "product_media";

                // Act
                String mediaUrl = imageServingUtil.saveMediaFile(file, folderName, "/mediaUrlCode/", true);

                // Assert
                assertNotNull(mediaUrl);
                assertTrue(mediaUrl.endsWith(".mp4"));

                // Construir ruta esperada del archivo
                String fileName = mediaUrl.substring(mediaUrl.lastIndexOf('/') + 1);
                Path savedFile = Paths.get(System.getProperty("user.dir"), folderName, fileName);

                assertTrue(Files.exists(savedFile));

                // Opcional: limpiar el archivo creado
                Files.deleteIfExists(savedFile);
        }

        @Test
        void testSaveImages_VideoNotAllowed() {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                                "video", "clip.mp4", "video/mp4", new byte[] { 0, 1, 2, 3 });

                // Act + Assert
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                () -> imageServingUtil.saveMediaFile(file, basePath, "/mediaUrlCode/", false));

                assertTrue(ex.getMessage().contains("Invalid file type. Only image types are allowed."));
        }

        @Test
        void testSaveImages_IOException() throws IOException {
                // Arrange
                MultipartFile file = mock(MultipartFile.class);
                when(file.getOriginalFilename()).thenReturn("photo.png");
                when(file.isEmpty()).thenReturn(false);
                when(file.getInputStream()).thenThrow(new IOException("Disk error"));
                when(file.getContentType()).thenReturn("image/png");

                String folderName = "product_media";

                // Act + Assert
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                () -> imageServingUtil.saveMediaFile(file, folderName, "/mediaUrlCode/", false));

                assertTrue(ex.getMessage().contains("Error while saving the file"));
        }

        @Test
        void testSaveImages_AllowVideoTrue_ValidVideo() throws IOException {
                MockMultipartFile file = new MockMultipartFile(
                                "video", "clip.mp4", "video/mp4", new byte[] { 1, 2, 3 });

                String folderName = "product_media";

                String url = imageServingUtil.saveMediaFile(file, folderName, "/media/", true);

                assertNotNull(url);
                assertTrue(url.endsWith(".mp4"));
        }

        @Test
        void testSaveImages_AllowVideoFalse_ValidVideo() {
                MockMultipartFile file = new MockMultipartFile(
                                "video", "clip.mp4", "video/mp4", new byte[] { 1, 2, 3 });

                String folderName = "product_media";

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                () -> imageServingUtil.saveMediaFile(file, folderName, "/media/", false));

                assertTrue(ex.getMessage().contains("Invalid file type. Only image types are allowed."));
        }

        @Test
        void testSaveImages_NullContentType() {
                MultipartFile file = mock(MultipartFile.class);
                when(file.getOriginalFilename()).thenReturn("file.bin");
                when(file.getContentType()).thenReturn(null);
                when(file.isEmpty()).thenReturn(false);

                String folderName = "product_media";

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                () -> imageServingUtil.saveMediaFile(file, folderName, "/media/", true));

                assertTrue(ex.getMessage().contains("Invalid file type. Allowed image or video types."));
        }

}
