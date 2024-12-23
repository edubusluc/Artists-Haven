package com.artists_heaven.product;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/product_media/";
    private static final Path TARGET_PATH = new File(UPLOAD_DIR).toPath().normalize();

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product registerProduct(ProductDTO productDTO) {
        try {
            Product product = new Product();
            product.setCategories(productDTO.getCategories());
            product.setDescription(productDTO.getDescription());
            product.setName(productDTO.getName());
            product.setPrice(productDTO.getPrice());
            product.setSize(productDTO.getSizes());
            product.setImages(productDTO.getImages());
            product.setAvailable(false);
            return productRepository.save(product);
        } catch (Exception e) {
            throw new IllegalArgumentException("No se ha podido crear el producto");
        }
    }

    public Set<Category> getAllCategories() {
        return productRepository.getAllCategories();
    }

    public List<String> saveImages(List<MultipartFile> images) {
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile image : images) {
            String fileName = StringUtils.cleanPath(image.getOriginalFilename());
            Path targetPath = Paths.get(UPLOAD_DIR, fileName).normalize();

            if (!targetPath.startsWith(TARGET_PATH)) {
                throw new IllegalArgumentException("Entry is outside of the target directory");
            }

            try {
                // Guardar la imagen en el directorio
                Files.copy(image.getInputStream(), targetPath);
                // Agregar la URL o nombre del archivo a la lista (ajustado según la necesidad)
                fileName = "/product_media/" + fileName;
                imageUrls.add(fileName); // O la URL completa si se está usando un servicio externo
            } catch (IOException e) {
                throw new IllegalArgumentException("Error al guardar las imágenes.");
            }
        }

        return imageUrls;
    }

    public List<String> deleteImages(List<MultipartFile> removedImages) {
        List<String> imagesToDelete = new ArrayList<>();

        for (MultipartFile image : removedImages) {
            String fileName = StringUtils.cleanPath(image.getOriginalFilename());
            Path targetPath = Paths.get(UPLOAD_DIR, fileName).normalize();

            if (!targetPath.startsWith(TARGET_PATH)) {
                throw new IllegalArgumentException("Entry is outside of the target directory");
            }

            try {
                Files.delete(targetPath);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error al eliminar las imágenes.");
            }
            fileName = "/product_media/" + fileName;
            imagesToDelete.add(fileName);
        }
        return imagesToDelete;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public void save(Product product) {
        productRepository.save(product);
    }

    public void updateProduct(Product product, List<MultipartFile> removedImages, List<MultipartFile> newImages, ProductDTO productDTO) {
        // Manejo de imágenes
        List<String> existingImages = product.getImages();

        if (removedImages != null && !removedImages.isEmpty()) {
            // Eliminar imágenes del servidor
            List<String> removedImagesCast = deleteImages(removedImages);

            // Filtrar las imágenes existentes para eliminar las indicadas
            existingImages.removeIf(removedImagesCast::contains);
        }

        if (newImages != null && !newImages.isEmpty()) {
            // Guardar nuevas imágenes en el servidor y obtener sus URLs
            List<String> newImageUrls = saveImages(newImages);

            // Combinar las imágenes existentes con las nuevas
            existingImages.addAll(newImageUrls);
        }

        // Actualizar el producto con las imágenes finales
        productDTO.setImages(existingImages);

        // Actualización de los demás datos del producto
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setSize(productDTO.getSizes());
        product.setCategories(productDTO.getCategories());

        // Guardar el producto actualizado
        save(product);
    }
}