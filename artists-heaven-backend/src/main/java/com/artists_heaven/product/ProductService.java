package com.artists_heaven.product;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/product_media/";
    private static final Path TARGET_PATH = new File(UPLOAD_DIR).toPath().normalize();

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Registers a new product based on the provided product data.
     *
     * @param productDTO the DTO containing the product details to be registered.
     * @return the registered product.
     * @throws IllegalArgumentException if there is an error while saving the
     *                                  product.
     */
    public Product registerProduct(ProductDTO productDTO) {
        try {
            // Create a new product instance
            Product product = new Product();

            // Set product attributes using data from the productDTO
            product.setCategories(productDTO.getCategories());
            product.setDescription(productDTO.getDescription());
            product.setName(productDTO.getName());
            product.setPrice(productDTO.getPrice());
            product.setSize(productDTO.getSizes());
            product.setImages(productDTO.getImages());
            // Save the product to the repository and return the saved product
            return productRepository.save(product);
        } catch (Exception e) {
            // Throw an exception if there is an error while creating the product
            throw new IllegalArgumentException("Unable to create the product");
        }
    }

    /**
     * Retrieves all categories associated with products.
     *
     * @return a set of all categories.
     */
    public Set<Category> getAllCategories() {
        // Fetch and return all categories from the product repository
        return productRepository.getAllCategories();
    }

    /**
     * Saves a list of images and returns a list of URLs where the images are
     * stored.
     *
     * @param images List of images to be saved.
     * @return List of image URLs after being saved.
     * @throws IllegalArgumentException if any of the images is invalid or if an
     *                                  error occurs during saving.
     */
    public List<String> saveImages(List<MultipartFile> images) {
        List<String> imageUrls = new ArrayList<>();
        // Validate that all images are valid before proceeding
        validateImages(images);

        // Iterate through each image and save it
        for (MultipartFile image : images) {
            // Clean the filename to ensure it is safe to use
            String fileName = StringUtils.cleanPath(image.getOriginalFilename());
            String extension = "";

            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = fileName.substring(dotIndex);
            }

            String uniqueFileName = UUID.randomUUID().toString() + extension;

            Path targetPath = Paths.get(UPLOAD_DIR, uniqueFileName).normalize();

            // Ensure the file is not saved outside the target directory for security
            if (!targetPath.startsWith(TARGET_PATH)) {
                throw new IllegalArgumentException("Entry is outside of the target directory");
            }

            try {
                // Save the image to the directory
                Files.copy(image.getInputStream(), targetPath);
                // Add the image's URL to the list (adjust the URL according to the system)
                imageUrls.add("/product_media/" + uniqueFileName);
            } catch (IOException e) {
                // Throw an exception if an error occurs while saving the image
                throw new IllegalArgumentException("Error while saving images.");
            }
        }

        // Return the list of image URLs after all images are saved
        return imageUrls;
    }

    /**
     * Validates the list of images to ensure none of them are empty.
     *
     * @param images List of images to be validated.
     * @throws IllegalArgumentException if any image is empty.
     */
    private void validateImages(List<MultipartFile> images) {
        // Iterate through the list of images
        for (MultipartFile image : images) {
            // Check if any image is empty
            if (image.isEmpty()) {
                // Throw an exception if an image is empty
                throw new IllegalArgumentException("Images cannot be empty.");
            }
        }
    }

    /**
     * Deletes the specified images and returns a list of the corresponding file
     * paths.
     *
     * @param removedImages List of images to be deleted.
     * @return List of file paths for the deleted images.
     * @throws IllegalArgumentException if any image is outside the target directory
     *                                  or if an error occurs during deletion.
     */
    public List<String> deleteImages(List<MultipartFile> removedImages) {
        List<String> imagesToDelete = new ArrayList<>();

        // Iterate through the list of images to be removed
        for (MultipartFile image : removedImages) {
            // Clean the image file name to avoid path traversal issues
            String fileName = StringUtils.cleanPath(image.getOriginalFilename());

            // Define the target path where the image is stored
            Path targetPath = Paths.get(UPLOAD_DIR, fileName).normalize();

            // Ensure the target path is within the allowed directory
            if (!targetPath.startsWith(TARGET_PATH)) {
                throw new IllegalArgumentException("Entry is outside of the target directory");
            }

            try {
                // Attempt to delete the image file
                Files.delete(targetPath);
            } catch (IOException e) {
                // Handle errors during the deletion process
                throw new IllegalArgumentException("Error deleting the images.");
            }

            // Add the image file path to the list of deleted images
            fileName = "/product_media/" + fileName;
            imagesToDelete.add(fileName);
        }

        // Return the list of deleted image paths
        return imagesToDelete;
    }

    /**
     * Retrieves all the products from the product repository.
     *
     * @return List of all products stored in the repository.
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        // Retrieve and return all products from the repository
        return productRepository.findAllProductsSortByName(pageable);
    }

    /**
     * Deletes a product by its ID from the product repository.
     *
     * @param id The ID of the product to be deleted.
     */
    public void deleteProduct(Long id) {
        // Delete the product with the given ID from the repository
        productRepository.deleteById(id);
    }

    /**
     * Finds a product by its ID.
     * Throws an exception if the product is not found.
     *
     * @param id The ID of the product to be found.
     * @return The product if found.
     * @throws IllegalArgumentException If no product with the given ID is found.
     */
    public Product findById(Long id) {
        // Attempt to find the product by its ID. If not found, throw an exception
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    /**
     * Saves a product to the repository.
     *
     * @param product The product to be saved.
     */
    public void save(Product product) {
        // Save the given product in the repository
        productRepository.save(product);
    }

    /**
     * Updates the product with new information, images, and removes selected
     * images.
     *
     * @param product       The product to be updated.
     * @param removedImages A list of images to be removed from the product.
     * @param newImages     A list of new images to be added to the product.
     * @param productDTO    The DTO containing the updated product information.
     */
    public void updateProduct(Product product, List<MultipartFile> removedImages, List<MultipartFile> newImages,
            ProductDTO productDTO) {
        // Get the current list of images associated with the product
        List<String> existingImages = product.getImages();

        // If there are removed images, delete them and update the existing images list
        if (removedImages != null && !removedImages.isEmpty()) {
            List<String> removedImagesCast = deleteImages(removedImages); // Delete images
            existingImages.removeIf(removedImagesCast::contains); // Remove from the list
        }

        // If there are new images, save them and add the new image URLs to the list
        if (newImages != null && !newImages.isEmpty()) {
            List<String> newImageUrls = saveImages(newImages); // Save new images
            existingImages.addAll(newImageUrls); // Add new image URLs to the existing images list
        }

        // Update the images in the product DTO
        productDTO.setImages(existingImages);

        // Update product details from the DTO
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setSize(productDTO.getSizes());
        product.setCategories(productDTO.getCategories());

        // Save the updated product
        save(product);
    }

    public void promoteProduct(Long productId, Integer discount) {
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }
        Product product = findById(productId);
        if (!product.getAvailable()) {
            throw new IllegalArgumentException("Product is not available");
        }

        if (product.getOn_Promotion()) {
            throw new IllegalArgumentException("Product is already on promotion");
        }
        product.setOn_Promotion(true);
        product.setDiscount(discount);
        Float calculatePrice = product.getPrice() * discount / 100;
        product.setPrice(product.getPrice() - calculatePrice);
        save(product);
    }

    public void demoteProduct(Long productId) {
        Product product = findById(productId);
        if (!product.getAvailable()) {
            throw new IllegalArgumentException("Product is not available");
        }
        if (!product.getOn_Promotion()) {
            throw new IllegalArgumentException("Product is not on promotion");
        }
        product.setOn_Promotion(false);
        Integer discount = product.getDiscount();
        Float productPrice = reCalculatePrice(discount, product.getPrice());
        product.setDiscount(0);
        product.setPrice(productPrice);
        save(product);
    }

    private Float reCalculatePrice(Integer discount, Float price) {

        Float reCalculatePrice = (100 - discount) / 100f;
        return price / reCalculatePrice;

    }

    public List<Product> getAllPromotedProducts() {
        return productRepository.findAllByOn_Promotion();
    }

    public Page<Product> searchProducts(String searchTerm, Pageable pageable) {
        return productRepository.findByName(searchTerm, pageable);
    }

    public List<Product> get12ProductsSortedByName() {
        return productRepository.find12ProductsSortedByName();
    }

    public List<Product> findAllByIds(Set<Long> productIds) {
        return productRepository.findAllById(productIds);
    }

    public void disableProduct(Long productId) {
        setProductAvailability(productId, false, "Product is already disabled");
    }

    public void enableProduct(Long productId) {
        setProductAvailability(productId, true, "Product is already enabled");
    }

    private void setProductAvailability(Long productId, boolean availability, String errorMessage) {
        Product product = findById(productId);
        if (product.getAvailable() == availability) {
            throw new IllegalArgumentException(errorMessage);
        }
        product.setAvailable(availability);
        productRepository.save(product);
    }

}