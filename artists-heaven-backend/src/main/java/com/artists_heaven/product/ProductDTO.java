package com.artists_heaven.product;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing the data required to create or update a
 * product.
 * Includes basic information like name, description, price, sizes, categories,
 * and images.
 */
@Getter
@Setter
@Schema(name = "ProductDTO", description = "Represents product data including name, description, price, sizes, categories, and images.")
public class ProductDTO {

    /**
     * Name of the product.
     */
    @Schema(description = "Name of the product", example = "Abstract Landscape Painting", required = true)
    @NotBlank(message = "{product.name.required}")
    @Size(max = 255, message = "{product.name.maxlength}")
    @Column(nullable = false)
    private String name;

    /**
     * Description of the product (up to 1000 characters).
     */
    @Size(max = 1000, message = "{product.description.maxlength}")
    @Schema(description = "Detailed description of the product", example = "A beautiful hand-painted abstract landscape in vivid colors.")
    private String description;

    /**
     * Price of the product (must be positive).
     */
    @NotNull(message = "{product.price.required}")
    @Positive(message = "{product.price.positive}")
    @Schema(description = "Price of the product", example = "129.99", required = true)
    private Float price;

    /**
     * Available sizes and their corresponding stock quantities.
     * Example: { "M": 10, "L": 5 }
     */
    @Schema(description = "Map of sizes to available quantities", example = "{ \"S\": 5, \"M\": 10, \"L\": 3 }")
    @Column(nullable = false)
    private Map<String, Integer> sizes;

    /**
     * Categories the product belongs to (e.g., Painting, Sculpture).
     */
    @Schema(description = "Set of categories associated with the product", example = "[ \"PAINTING\", \"MODERN_ART\" ]")
    private Set<Category> categories;

    private Long collectionId;

    /**
     * List of image URLs associated with the product.
     */
    @Schema(description = "List of image URLs or filenames for the product", example = "[ \"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\" ]")
    @Size(min = 1, message = "{product.images.required}")
    private List<String> images;

    private Date createdDate;

    private Long id;

    private Boolean onPromotion;

    @Min(value = 0, message = "{product.discount.min}")
    @Max(value = 100, message = "{product.discount.max}")
    private Integer discount;

    @NotNull(message = "{product.section.required}")
    private Section section;

    @Min(value = 0, message = "{product.availableUnits.min}")
    private Integer availableUnits;

    private Boolean available;

    private Long reference;

    @NotBlank(message = "{product.composition.required}")
    private String composition;

    @NotBlank(message = "{product.shippingDetails.required}")
    private String shippingDetails;

    private String modelReference;

    /**
     * Constructor that maps a Product entity to ProductDTO.
     *
     * @param product the Product entity to map.
     */
    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.sizes = product.getSize();
        this.categories = product.getCategories();
        this.images = product.getImages();
        this.createdDate = product.getCreatedDate();
        this.onPromotion = product.getOn_Promotion();
        this.discount = product.getDiscount();
        this.section = product.getSection();
        this.availableUnits = product.getAvailableUnits();
        this.available = product.getAvailable();
        this.reference = product.getReference();
        this.composition = product.getComposition();
        this.shippingDetails = product.getShippingDetails();
        this.collectionId = product.getCollection() != null ? product.getCollection().getId() : null;
        this.modelReference = product.getModelReference();

    }

    public ProductDTO() {

    }
}
