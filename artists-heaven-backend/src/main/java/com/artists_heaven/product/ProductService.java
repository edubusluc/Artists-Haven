package com.artists_heaven.product;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.artists_heaven.admin.CategoryDTO;
import com.artists_heaven.admin.CollectionDTO;
import com.artists_heaven.exception.AppExceptions;
import com.artists_heaven.exception.AppExceptions.BadRequestException;
import com.artists_heaven.exception.AppExceptions.InvalidInputException;
import com.artists_heaven.exception.AppExceptions.ResourceNotFoundException;
import com.artists_heaven.order.Order;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.page.PageResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    private final CollectionRepository collectionRepository;

    private final MessageSource messageSource;

    private static final ThreadLocalRandom TL_RANDOM = ThreadLocalRandom.current();

    private static final String UPLOAD_DIR = "artists-heaven-backend/src/main/resources/product_media/";
    private static final Path TARGET_PATH = new File(UPLOAD_DIR).toPath().normalize();

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
            CollectionRepository collectionRepository, MessageSource messageSource) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.collectionRepository = collectionRepository;
        this.messageSource = messageSource;
    }

    /**
     * Registers a new product based on the provided product data.
     *
     * @param productDTO the DTO containing the product details to be registered.
     * @return the registered product.
     * @throws IllegalArgumentException if there is an error while saving the
     *                                  product.
     */
    // Uso intencional de PRNG no criptográfico: referencia visible, no sensible
    // (java:S2245)
    public Product registerProduct(ProductDTO productDTO) {
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new AppExceptions.InvalidInputException("Product name is required.");
        }
        if (productDTO.getPrice() == null || productDTO.getPrice().doubleValue() <= 0) {
            throw new AppExceptions.InvalidInputException("Product price must be greater than 0.");
        }

        Product product = new Product();
        product.setCategories(productDTO.getCategories());
        product.setDescription(productDTO.getDescription());
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setImages(productDTO.getImages());
        product.setSection(productDTO.getSection());
        product.setComposition(productDTO.getComposition());
        product.setShippingDetails(productDTO.getShippingDetails());
        product.setCollection(productDTO.getCollectionId() != null
                ? collectionRepository.findById(productDTO.getCollectionId())
                        .orElseThrow(() -> new AppExceptions.ResourceNotFoundException(
                                "Collection not found with id: " + productDTO.getCollectionId()))
                : null);
        product.setModelReference(productDTO.getModelReference());

        if (productDTO.getAvailableUnits() != 0) {
            product.setAvailableUnits(productDTO.getAvailableUnits());
        } else {
            product.setSize(productDTO.getSizes());
        }

        for (int i = 0; i < 5; i++) { // reintenta algunas veces
            product.setReference(generarReferencia());
            try {
                return productRepository.save(product);
            } catch (DataIntegrityViolationException e) {
                // colisión de referencia: reintenta
            }
        }
        throw new IllegalStateException("A unique reference could not be generated after several attempts.");
    }

    private long generarReferencia() {
        return TL_RANDOM.nextLong(10_000L, 1_000_000_000L);
    }

    /**
     * Retrieves all categories associated with products.
     *
     * @return a set of all categories.
     */
    public Set<Category> getAllCategories() {
        Set<Category> categories = productRepository.getAllCategories();
        if (categories.isEmpty()) {
            throw new AppExceptions.ResourceNotFoundException("No categories found");
        }
        return categories;
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

    public void deleteImagesString(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            try {
                // Extraer solo el nombre del archivo
                String fileName = Paths.get(imageUrl).getFileName().toString();

                // Construir la ruta absoluta
                Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();

                // Eliminar físicamente el archivo
                Files.deleteIfExists(filePath);

            } catch (IOException e) {
                throw new RuntimeException("Error eliminando la imagen: " + imageUrl, e);
            }
        }
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
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
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
    public void updateProduct(Long id,
            List<MultipartFile> removedImages,
            List<MultipartFile> newImages,
            ProductDTO productDTO,
            List<String> reorderedImages) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Product not found with id: " + id));

        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new AppExceptions.InvalidInputException("Product name cannot be empty");
        }
        if (productDTO.getPrice() == null || productDTO.getPrice().doubleValue() <= 0) {
            throw new AppExceptions.InvalidInputException("Product price must be greater than 0");
        }

        // --- Manejo de imágenes ---
        List<String> existingImages = new ArrayList<>(product.getImages());

        if (removedImages != null && !removedImages.isEmpty()) {
            List<String> removedImagesCast = deleteImages(removedImages);
            existingImages.removeIf(removedImagesCast::contains);
        }

        if (newImages != null && !newImages.isEmpty()) {
            List<String> newImageUrls = saveImages(newImages);
            existingImages.addAll(newImageUrls);
        }

        List<String> finalOrderedImages;
        if (reorderedImages != null && !reorderedImages.isEmpty()) {
            finalOrderedImages = reorderedImages.stream()
                    .filter(existingImages::contains)
                    .distinct()
                    .collect(Collectors.toList());

            existingImages.stream()
                    .filter(img -> !finalOrderedImages.contains(img))
                    .forEach(finalOrderedImages::add);
        } else {
            finalOrderedImages = existingImages;
        }

        product.setImages(finalOrderedImages);
        productDTO.setImages(finalOrderedImages);

        // --- Actualización de datos ---
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.getSize().clear();
        product.getSize().putAll(productDTO.getSizes());
        product.setSize(productDTO.getSizes());
        product.getCategories().clear();
        product.getCategories().addAll(productDTO.getCategories());
        product.setComposition(productDTO.getComposition());
        product.setShippingDetails(productDTO.getShippingDetails());
        product.setCollection(productDTO.getCollectionId() != null
                ? collectionRepository.findById(productDTO.getCollectionId())
                        .orElseThrow(() -> new AppExceptions.ResourceNotFoundException(
                                "Collection not found with id: " + productDTO.getCollectionId()))
                : null);

        save(product);
    }

    public void promoteProduct(Long productId, Integer discount) {
        if (discount == null || discount < 0 || discount > 100) {
            throw new InvalidInputException("Discount must be between 0 and 100");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.getAvailable()) {
            throw new InvalidInputException("Product is not available");
        }

        if (Boolean.TRUE.equals(product.getOn_Promotion())) {
            throw new InvalidInputException("Product is already on promotion");
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
            throw new InvalidInputException("Product is not available");
        }
        if (!product.getOn_Promotion()) {
            throw new InvalidInputException("Product is not on promotion");
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
            throw new BadRequestException(errorMessage);
        }
        product.setAvailable(availability);
        productRepository.save(product);
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Collection> findAllCollections() {
        return collectionRepository.findAll();
    }

    public void saveCategory(String name) {
        // Verificar si la categoría ya existe
        if (categoryRepository.existsByName(name)) {
            // Lanzar una excepción si la categoría ya existe
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Category with the name '" + name + "' already exists.");
        }

        // Si no existe, proceder a crear la nueva categoría
        Category newCategory = new Category();
        newCategory.setName(name);

        // Guardar la nueva categoría en la base de datos
        categoryRepository.save(newCategory);
    }

    public void editCategory(CategoryDTO categoryDTO) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryDTO.getId());
        if (!optionalCategory.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Category with the id '" + categoryDTO.getId() + "' not exists.");
        }
        Category category = optionalCategory.get();
        category.setName(categoryDTO.getName().replaceAll("\s+", ""));
        categoryRepository.save(category);
    }

    public List<Product> findProductsByArtist(String artistName) {
        if (artistName == null || artistName.isEmpty()) {
            return Collections.emptyList();
        }
        return productRepository.findProductsByArtistCategory(artistName);
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> findTshirtsProduct() {
        return productRepository.findBySection(Section.TSHIRT);
    }

    public List<Product> findHoodiesProduct() {
        return productRepository.findBySection(Section.HOODIES);
    }

    public List<Product> findPantsProduct() {
        return productRepository.findBySection(Section.PANTS);
    }

    public List<Product> findAccessoriesProduct() {
        return productRepository.findBySection(Section.ACCESSORIES);
    }

    public Map<String, String> getTopSellingProduct() {
        List<Order> orders = productRepository.getOrders();

        Map<String, Integer> countMap = new HashMap<>();
        String topProduct = null;
        int max = 0;

        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                String name = item.getName();
                int total = countMap.merge(name, 1, Integer::sum);
                if (total > max) {
                    max = total;
                    topProduct = name;
                }
            }
        }

        Product product = productRepository.findByNameIgnoreCase(topProduct);

        String price = formatPrice(product.getPrice());

        return Map.of(
                "nombre", sanitize(product.getName()),
                "descripcion", sanitize(product.getDescription()),
                "precio", price);
    }

    // ✅ Método para formatear precio de forma segura
    private String formatPrice(Float price) {
        if (price == null) {
            return "0.00";
        }
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(price);
    }

    // ✅ Método para sanitizar strings (por si se usan en vistas o logs)
    private String sanitize(String input) {
        return input == null ? "" : input.replaceAll("%", "%%");
    }

    public Map<String, String> getRecommendedProduct() {
        List<Product> products = productRepository.findTopRatingProduct();

        // Asegurarse de que la lista tiene al menos 3 productos
        int limit = Math.min(3, products.size());
        List<Product> top3Products = products.subList(0, limit);

        Map<String, String> recommendedMap = new LinkedHashMap<>();

        for (Product product : top3Products) {
            String name = product.getName();
            String description = product.getDescription();
            double price = product.getPrice();

            // Puedes personalizar el formato del valor como desees
            String value = "Descripción: " + description + ", Precio: $" + price;

            recommendedMap.put(name, value);
        }

        return recommendedMap;
    }

    public List<Product> getRelatedProducts(String sectionName, Long id) {
        Section section;
        try {
            section = Section.valueOf(sectionName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid section: " + sectionName);
        }

        return productRepository.findBySection(section).stream()
                .filter(product -> !product.getId().equals(id))
                .filter(Product::getAvailable)
                .sorted(Comparator.comparing(Product::getCreatedDate).reversed())
                .limit(4)
                .toList();
    }

    public Product findByReference(Long reference, String lang) {
        Locale locale = new Locale(lang);

        Optional<Product> product = productRepository.findByReference(reference);
        if (!product.isPresent()) {
            String msg = messageSource.getMessage("product.notReference", null, locale);
            throw new AppExceptions.ResourceNotFoundException(msg + reference);
        } else {
            return product.get();
        }
    }

    public void saveCollection(String name) {
        if (collectionRepository.existsByName(name)) {
            // Lanzar una excepción si la categoría ya existe
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Category with the name '" + name + "' already exists.");
        }

        // Si no existe, proceder a crear la nueva categoría
        Collection newCollection = new Collection();
        newCollection.setName(name.toUpperCase());

        // Guardar la nueva categoría en la base de datos
        collectionRepository.save(newCollection);
    }

    public void editCollection(CollectionDTO collectionDTO) {
        Optional<Collection> optionalCollection = collectionRepository.findById(collectionDTO.getId());
        if (!optionalCollection.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Collection with the id '" + collectionDTO.getId() + "' not exists.");
        }
        Collection collection = optionalCollection.get();
        collection.setName(collectionDTO.getName().replaceAll("\s+", ""));
        collection.setIsPromoted(collectionDTO.getIsPromoted());
        collectionRepository.save(collection);
    }

    public List<Product> findByCollection(String collectionName) {
        return productRepository.findByCollectionName(collectionName);
    }

    public PageResponse<ProductDTO> getProducts(int page, int size, String search) {
        if (size == -1) {
            List<Product> products = findAllProducts();

            // Convertimos todos los Product a ProductDTO
            List<ProductDTO> productDTOs = products.stream()
                    .map(ProductDTO::new)
                    .collect(Collectors.toList());

            return new PageResponse<>(
                    productDTOs,
                    0,
                    productDTOs.size(),
                    productDTOs.size(),
                    1,
                    true);
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Product> result;

        if (search != null && !search.isEmpty()) {
            result = searchProducts(search, pageRequest);
        } else {
            result = productRepository.findAll(pageRequest);
        }

        // Convertimos el contenido del Page<Product> a Page<ProductDTO>
        Page<ProductDTO> dtoPage = result.map(ProductDTO::new);

        return new PageResponse<>(dtoPage);
    }

    public String saveModel(MultipartFile modelFile) {
        // Validate that the model file is valid before proceeding
        validateModel(modelFile);

        // Clean the filename to ensure it is safe to use
        String fileName = StringUtils.cleanPath(modelFile.getOriginalFilename());
        String extension = "";

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex);
        }

        // You can customize the file extensions you accept for 3D models (e.g., .obj,
        // .fbx, .stl)
        List<String> allowedExtensions = Arrays.asList(".obj", ".fbx", ".stl", ".glb");
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Invalid file type. Only .obj, .fbx, .stl, .glb files are allowed.");
        }

        // Generate a unique file name for the model
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        // Define the path where the model will be saved
        Path targetPath = Paths.get(UPLOAD_DIR, uniqueFileName).normalize();

        // Ensure the file is not saved outside of the target directory for security
        if (!targetPath.startsWith(TARGET_PATH)) {
            throw new IllegalArgumentException("Entry is outside of the target directory");
        }

        try {
            // Save the model to the directory
            Files.copy(modelFile.getInputStream(), targetPath);

            // Return the URL or path for the saved model (adjust the URL accordingly)
            return "/product_media/" + uniqueFileName;
        } catch (IOException e) {
            // Throw an exception if an error occurs while saving the model
            throw new IllegalArgumentException("Error while saving the model.");
        }
    }

    // Helper method to validate the model file
    private void validateModel(MultipartFile modelFile) {
        if (modelFile == null || modelFile.isEmpty()) {
            throw new IllegalArgumentException("The model file is empty or invalid.");
        }
    }

}