package com.artists_heaven.product;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
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
import java.util.Objects;
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
import com.artists_heaven.order.OrderDetailsDTO;
import com.artists_heaven.order.OrderItem;
import com.artists_heaven.page.PageResponse;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    private final CollectionRepository collectionRepository;

    private final MessageSource messageSource;

    private static final SecureRandom TL_RANDOM = new SecureRandom();

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
    public Product registerProduct(@Valid ProductDTO productDTO) {

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
        product.setSection(productDTO.getSection());
        product.setComposition(productDTO.getComposition());
        product.setShippingDetails(productDTO.getShippingDetails());
        product.setCollection(
                productDTO.getCollectionId() != null
                        ? collectionRepository.findById(productDTO.getCollectionId())
                                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException(
                                        "Collection not found with id: " + productDTO.getCollectionId()))
                        : null);

        if (productDTO.getColors() == null || productDTO.getColors().isEmpty()) {
            throw new AppExceptions.InvalidInputException("Debe haber al menos un color con inventario e imágenes.");
        }

        List<ProductColor> colors = productDTO.getColors().stream().map(dto -> {
            ProductColor color = new ProductColor();
            color.setColorName(dto.getColorName());
            color.setHexCode(dto.getHexCode());
            color.setImages(dto.getImages());
            color.setProduct(product);
            color.setModelReference(dto.getModelReference());

            if (productDTO.getSection() == Section.ACCESSORIES) {
                if (dto.getAvailableUnits() == null || dto.getAvailableUnits() < 0) {
                    throw new AppExceptions.InvalidInputException(
                            "Cada color de un accesorio debe tener availableUnits >= 0.");
                }
                color.setAvailableUnits(dto.getAvailableUnits());
                color.setSizes(null);
            } else {
                if (dto.getSizes() == null || dto.getSizes().isEmpty()) {
                    throw new AppExceptions.InvalidInputException(
                            "Cada color de un producto no accesorio debe tener un mapa de tallas.");
                }
                dto.getSizes().forEach((size, qty) -> {
                    if (qty == null || qty < 0) {
                        throw new AppExceptions.InvalidInputException(
                                "Las cantidades por talla no pueden ser negativas.");
                    }
                });
                color.setSizes(dto.getSizes());
                color.setAvailableUnits(null);
            }
            return color;
        }).collect(Collectors.toList());
        product.setColors(colors);

        boolean hasStock = colors.stream().anyMatch(c -> {
            if (productDTO.getSection() == Section.ACCESSORIES) {
                return c.getAvailableUnits() != null && c.getAvailableUnits() > 0;
            } else {
                return c.getSizes() != null &&
                        c.getSizes().values().stream().mapToInt(Integer::intValue).sum() > 0;
            }
        });
        product.setAvailable(hasStock);

        for (int i = 0; i < 10; i++) {
            Long generatedReference = generarReferencia();
            product.setReference(generatedReference);
            try {
                return productRepository.save(product);
            } catch (DataIntegrityViolationException e) {

            }
        }

        throw new IllegalStateException("No se pudo generar una referencia única después de varios intentos.");
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

    /**
     * Retrieves detailed information about a specific order by its ID.
     * 
     * Access is restricted based on the currently authenticated user:
     * 
     * Administrators can access any order.
     * Regular users can only access their own orders.
     * If the order is unassigned (no user), only administrators can access it.
     * 
     *
     * @param id the ID of the order to retrieve
     * @return an {@link OrderDetailsDTO} containing detailed information about the
     *         order
     * @throws AppExceptions.ResourceNotFoundException if no order exists with the
     *                                                 given ID
     * @throws AppExceptions.ForbiddenActionException  if the authenticated user
     *                                                 does not have permission to
     *                                                 access the order
     */
    public void deleteImagesString(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            try {
                String fileName = Paths.get(imageUrl).getFileName().toString();

                Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();

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
    public void updateProduct(
            Long id,
            ProductDTO productDTO,
            Map<Integer, List<MultipartFile>> colorImages,
            Map<Integer, MultipartFile> colorModels) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Product not found with id: " + id));

        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new AppExceptions.InvalidInputException("Product name cannot be empty");
        }
        if (productDTO.getPrice() == null || productDTO.getPrice().doubleValue() <= 0) {
            throw new AppExceptions.InvalidInputException("Product price must be greater than 0");
        }

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.getCategories().clear();
        product.getCategories().addAll(productDTO.getCategories());
        product.setComposition(productDTO.getComposition());
        product.setShippingDetails(productDTO.getShippingDetails());
        product.setCollection(productDTO.getCollectionId() != null
                ? collectionRepository.findById(productDTO.getCollectionId())
                        .orElseThrow(() -> new AppExceptions.ResourceNotFoundException(
                                "Collection not found with id: " + productDTO.getCollectionId()))
                : null);
        product.setAvailable(productDTO.getAvailable());

        Map<Long, ProductColor> existingColorsById = product.getColors().stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(ProductColor::getId, c -> c));

        Set<Long> incomingIds = productDTO.getColors().stream()
                .map(ProductColorDTO::getColorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        product.getColors().removeIf(c -> c.getId() != null && !incomingIds.contains(c.getId()));

        for (int i = 0; i < productDTO.getColors().size(); i++) {
            ProductColorDTO colorDTO = productDTO.getColors().get(i);
            ProductColor color;

            if (colorDTO.getColorId() != null && existingColorsById.containsKey(colorDTO.getColorId())) {
                color = existingColorsById.get(colorDTO.getColorId());
            } else {
                color = new ProductColor();
                color.setProduct(product);
                product.getColors().add(color);
            }

            color.setColorName(colorDTO.getColorName());
            color.setHexCode(colorDTO.getHexCode());
            color.setAvailableUnits(colorDTO.getAvailableUnits());
            color.setSizes(colorDTO.getSizes());

            List<String> finalImages = new ArrayList<>(
                    colorDTO.getImages() != null ? colorDTO.getImages() : new ArrayList<>());
            if (colorImages != null && colorImages.containsKey(i)) {
                List<MultipartFile> newImgs = colorImages.get(i);
                List<String> newUrls = saveImages(newImgs);
                finalImages.addAll(newUrls);
            }
            color.setImages(finalImages);

            if (colorModels != null && colorModels.containsKey(i)) {
                MultipartFile modelFile = colorModels.get(i);
                if (modelFile != null && !modelFile.isEmpty()) {
                    String modelUrl = saveModel(modelFile);
                    color.setModelReference(modelUrl);
                }
            } else {
                color.setModelReference(colorDTO.getModelReference());
            }
        }

        save(product);
    }

    /**
     * Promotes a product by applying a discount.
     * 
     * @param productId the ID of the product to promote
     * @param discount  the discount percentage (0-100)
     * @throws InvalidInputException     if discount is null, out of range, or the
     *                                   product is not available
     * @throws ResourceNotFoundException if the product does not exist
     */
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

    /**
     * Removes the promotion from a product and recalculates its price.
     * 
     * @param productId the ID of the product to demote
     * @throws InvalidInputException if the product is not available or not on
     *                               promotion
     */
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

    /**
     * Retrieves all products that are currently on promotion.
     * 
     * @return a list of promoted products
     */
    public List<Product> getAllPromotedProducts() {
        return productRepository.findAllByOn_Promotion();
    }

    /**
     * Searches products by name or description with pagination support.
     * 
     * @param searchTerm the term to search for
     * @param pageable   pagination information
     * @return a page of products matching the search
     */
    public Page<Product> searchProducts(String searchTerm, Pageable pageable) {
        return productRepository.findByName(searchTerm, pageable);
    }

    /**
     * Retrieves the first 12 products sorted alphabetically by name.
     * 
     * @return a list of products
     */
    public List<Product> get12ProductsSortedByName() {
        return productRepository.find12ProductsSortedByName();
    }

    /**
     * Finds all products by a set of IDs.
     * 
     * @param productIds the set of product IDs
     * @return a list of products
     */
    public List<Product> findAllByIds(Set<Long> productIds) {
        return productRepository.findAllById(productIds);
    }

    /**
     * Disables a product.
     * 
     * @param productId the ID of the product to disable
     * @throws BadRequestException if the product is already disabled
     */
    public void disableProduct(Long productId) {
        setProductAvailability(productId, false, "Product is already disabled");
    }

    /**
     * Enables a product.
     * 
     * @param productId the ID of the product to enable
     * @throws BadRequestException if the product is already enabled
     */
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

    /**
     * Retrieves all categories.
     * 
     * @return a list of categories
     */
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Retrieves all collections.
     * 
     * @return a list of collections
     */
    public List<Collection> findAllCollections() {
        return collectionRepository.findAll();
    }

    /**
     * Saves a new category.
     * 
     * @param name the name of the category
     * @throws ResponseStatusException if the category already exists
     */
    public void saveCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new AppExceptions.BadRequestException(
                    "Category with the name '" + name + "' already exists.");
        }

        Category newCategory = new Category();
        newCategory.setName(name);

        categoryRepository.save(newCategory);
    }

    /**
     * Edits an existing category.
     * 
     * @param categoryDTO the category data transfer object containing new data
     * @throws ResponseStatusException if the category does not exist
     */
    public void editCategory(CategoryDTO categoryDTO) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryDTO.getId());
        if (!optionalCategory.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Category with the id '" + categoryDTO.getId() + "' not exists.");
        }
        String newName = categoryDTO.getName().replaceAll("\\s+", "");
        if (categoryRepository.existsByName(newName) &&
                !optionalCategory.get().getName().equalsIgnoreCase(newName)) {
            throw new AppExceptions.BadRequestException(
                    "Category with the name '" + newName + "' already exists.");
        }
        Category category = optionalCategory.get();
        category.setName(categoryDTO.getName().replaceAll("\s+", ""));
        categoryRepository.save(category);
    }

    /**
     * Finds products by an artist name.
     * 
     * @param artistName the name of the artist
     * @return a list of products, empty if artistName is null or empty
     */
    public List<Product> findProductsByArtist(String artistName) {
        if (artistName == null || artistName.isEmpty()) {
            return Collections.emptyList();
        }
        return productRepository.findProductsByArtistCategory(artistName);
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Finds products by section TSHIRT.
     * 
     * @return a list of products in the given section
     */
    public List<Product> findTshirtsProduct() {
        return productRepository.findBySection(Section.TSHIRT);
    }

    /**
     * Finds products by section HOODIES
     * 
     * @return a list of products in the given section
     */
    public List<Product> findHoodiesProduct() {
        return productRepository.findBySection(Section.HOODIES);
    }

    /**
     * Finds products by section PANTS
     * 
     * @return a list of products in the given section
     */
    public List<Product> findPantsProduct() {
        return productRepository.findBySection(Section.PANTS);
    }

    /**
     * Finds products by section ACCESSORIES
     * 
     * @return a list of products in the given section
     */
    public List<Product> findAccessoriesProduct() {
        return productRepository.findBySection(Section.ACCESSORIES);
    }

    /**
     * Retrieves the top-selling product based on the number of times it has been
     * ordered.
     * 
     * This method iterates through all available orders, counts the number of times
     * each product has been sold, and identifies the product with the highest
     * sales.
     * It then returns basic information about this top-selling product.
     *
     * @return a map containing:
     *         - "nombre": the name of the top-selling product (sanitized)
     *         - "descripcion": the description of the product (sanitized)
     *         - "precio": the product price formatted as a string with two decimal
     *         places
     * @throws NullPointerException if there are no orders or the top product cannot
     *                              be found in the database
     */
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

    private String formatPrice(Float price) {
        if (price == null) {
            return "0.00";
        }
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(price);
    }

    private String sanitize(String input) {
        return input == null ? "" : input.replaceAll("%", "%%");
    }

    /**
     * Retrieves the top 3 recommended products based on their rating.
     *
     * This method fetches products with the highest ratings from the repository,
     * limits the results to a maximum of 3 products, and constructs a map where
     * the key is the product name and the value is a string with the description
     * and price.
     *
     * @return a LinkedHashMap where:
     *         - key: product name
     *         - value: "Description: <description>, Price: $<price>"
     */
    public Map<String, String> getRecommendedProduct() {
        List<Product> products = productRepository.findTopRatingProduct();

        int limit = Math.min(3, products.size());
        List<Product> top3Products = products.subList(0, limit);

        Map<String, String> recommendedMap = new LinkedHashMap<>();

        for (Product product : top3Products) {
            String name = product.getName();
            String description = product.getDescription();
            double price = product.getPrice();

            String value = "Descripción: " + description + ", Precio: $" + price;

            recommendedMap.put(name, value);
        }

        return recommendedMap;
    }

    /**
     * Retrieves related products from the same section, excluding the product with
     * the specified ID.
     *
     * The products are filtered to only include available items, sorted by creation
     * date in descending order,
     * and limited to 4 results.
     *
     * @param sectionName the name of the section to filter products by
     * @param id          the ID of the product to exclude
     * @return a list of related products
     * @throws InvalidInputException if the sectionName does not match a valid
     *                               Section enum
     */
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

    /**
     * Finds a product by its reference identifier.
     *
     * @param reference the reference ID of the product
     * @param lang      the language code for localized error messages
     * @return the product corresponding to the reference
     * @throws AppExceptions.ResourceNotFoundException if no product is found with
     *                                                 the given reference
     */
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

    /**
     * Saves a new collection with the specified name.
     *
     * @param name the name of the new collection
     * @throws ResponseStatusException if a collection with the same name already
     *                                 exists
     */
    public void saveCollection(String name) {
        if (collectionRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Category with the name '" + name + "' already exists.");
        }

        Collection newCollection = new Collection();
        newCollection.setName(name.toUpperCase());

        collectionRepository.save(newCollection);
    }

    /**
     * Edits an existing collection based on the provided DTO.
     *
     * Updates the collection name and promotion status.
     *
     * @param collectionDTO the DTO containing the collection's ID, new name, and
     *                      promotion status
     * @throws ResponseStatusException if the collection with the specified ID does
     *                                 not exist
     */
    public void editCollection(CollectionDTO collectionDTO) {
        Optional<Collection> optionalCollection = collectionRepository.findById(collectionDTO.getId());
        if (!optionalCollection.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Collection with the id '" + collectionDTO.getId() + "' not exists.");
        }

        String newName = collectionDTO.getName().replaceAll("\\s+", "");
        if (categoryRepository.existsByName(newName) &&
                !optionalCollection.get().getName().equalsIgnoreCase(newName)) {
            throw new AppExceptions.BadRequestException(
                    "Collection with the name '" + newName + "' already exists.");
        }
        Collection collection = optionalCollection.get();
        collection.setName(collectionDTO.getName().replaceAll("\s+", ""));
        collection.setIsPromoted(collectionDTO.getIsPromoted());
        collectionRepository.save(collection);
    }

    /**
     * Finds products by collection name.
     * 
     * @param collectionName the name of the collection
     * @return a list of products in the collection
     */
    public List<Product> findByCollection(String collectionName) {
        return productRepository.findByCollectionName(collectionName);
    }

    public PageResponse<ProductDTO> getProducts(int page, int size, String search, Boolean available,
            Boolean promoted) {
        if (size == -1) {
            List<Product> products = findAllProducts();

            // Convertimos todos los Product a ProductDTO
            List<ProductDTO> productDTOs = products.stream()
                    .map(ProductDTO::new)
                    .toList();

            return new PageResponse<>(
                    productDTOs,
                    0,
                    productDTOs.size(),
                    productDTOs.size(),
                    1,
                    true);
        }
        PageRequest pageRequest = PageRequest.of(page, size);

        Specification<Product> spec = Specification.where(null);

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%")));
        }

        if (available != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("available"), available));
        }

        if (promoted != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("on_Promotion"), promoted));
        }

        Page<Product> result = productRepository.findAll(spec, pageRequest);

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