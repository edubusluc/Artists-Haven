package com.artists_heaven.product;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object representing the data required to create or update a
 * product.
 * <p>
 * This DTO contains all relevant information about a product, including its
 * name, description, price,
 * available sizes and quantities, categories, images, and other metadata.
 * </p>
 * <p>
 * Fields:
 * <ul>
 * <li>{@code id}: Unique identifier of the product.</li>
 * <li>{@code name}: Name of the product (required, max 255 characters).</li>
 * <li>{@code description}: Detailed description of the product (optional, max
 * 1000 characters).</li>
 * <li>{@code price}: Price of the product (required, positive).</li>
 * <li>{@code categories}: Set of categories associated with the product.</li>
 * <li>{@code images}: List of image URLs or filenames (at least one
 * required).</li>
 * <li>{@code collectionId}: Identifier of the collection this product belongs
 * to (optional).</li>
 * <li>{@code createdDate}: Date when the product was created.</li>
 * <li>{@code onPromotion}: Indicates whether the product is on promotion.</li>
 * <li>{@code discount}: Discount percentage (0-100) if on promotion.</li>
 * <li>{@code section}: Section of the store where the product belongs
 * (required).</li>
 * <li>{@code available}: Whether the product is currently available for
 * purchase.</li>
 * <li>{@code reference}: Optional reference number for the product.</li>
 * <li>{@code composition}: Composition details of the product (required).</li>
 * <li>{@code shippingDetails}: Shipping instructions or details
 * (required).</li>
 * <li>{@code modelReference}: Optional model reference for the product.</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
@Schema(name = "ProductDTO", description = "Represents product data including name, description, price, sizes, categories, and images.")
@NoArgsConstructor
public class ProductDTO {

    @Schema(description = "Name of the product", example = "Abstract Landscape Painting")
    @NotBlank(message = "{product.name.required}")
    @Size(max = 255, message = "{product.name.maxlength}")
    @Column(nullable = false)
    private String name;

    @Schema(description = "Detailed description of the product", example = "A beautiful hand-painted abstract landscape in vivid colors.")
    @Size(max = 1000, message = "{product.description.maxlength}")
    private String description;

    @Schema(description = "Price of the product", example = "129.99")
    @NotNull(message = "{product.price.required}")
    @Positive(message = "{product.price.positive}")
    private Float price;

    @Schema(description = "Set of categories associated with the product", example = "[ \"PAINTING\", \"MODERN_ART\" ]")
    private Set<Category> categories;

    @Schema(description = "Identifier of the collection this product belongs to", example = "12")
    private Long collectionId;

    @Schema(description = "Date when the product was created", example = "2025-08-01T10:30:00Z")
    private Date createdDate;

    @Schema(description = "Unique identifier of the product", example = "101")
    private Long id;

    @Schema(description = "Indicates if the product is currently on promotion", example = "true")
    private Boolean onPromotion;

    @Schema(description = "Discount percentage applied to the product (0-100)", example = "20")
    @Min(value = 0, message = "{product.discount.min}")
    @Max(value = 100, message = "{product.discount.max}")
    private Integer discount;

    @Schema(description = "Section of the store where the product belongs", example = "PAINTINGS")
    @NotNull(message = "{product.section.required}")
    private Section section;

    @Schema(description = "Whether the product is currently available", example = "true")
    private Boolean available;

    @Schema(description = "Optional reference number for the product", example = "REF12345")
    private Long reference;

    @Schema(description = "Composition details of the product", example = "Canvas and acrylic paint")
    @NotBlank(message = "{product.composition.required}")
    private String composition;

    @Schema(description = "Shipping instructions or details", example = "Ships within 3-5 business days")
    @NotBlank(message = "{product.shippingDetails.required}")
    private String shippingDetails;

    @Schema(description = "Optional model 3dreference for the product", example = "tshirt.glb")
    private String modelReference;

    @Schema(description = "List of color variants for the product")
    private List<ProductColorDTO> colors;

    /**
     * Constructor that maps a {@link Product} entity to {@code ProductDTO}.
     * 
     * @param product the {@code Product} entity to map.
     */
    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.categories = product.getCategories();
        this.createdDate = product.getCreatedDate();
        this.onPromotion = product.getOn_Promotion();
        this.discount = product.getDiscount();
        this.section = product.getSection();
        this.available = product.getAvailable();
        this.reference = product.getReference();
        this.composition = product.getComposition();
        this.shippingDetails = product.getShippingDetails();
        this.collectionId = product.getCollection() != null ? product.getCollection().getId() : null;

        this.colors = product.getColors().stream().map(c -> {
            ProductColorDTO dto = new ProductColorDTO();
            dto.setColorName(c.getColorName());
            dto.setHexCode(c.getHexCode());
            dto.setImages(c.getImages());
            dto.setSizes(c.getSizes());
            dto.setAvailableUnits(c.getAvailableUnits());
            dto.setModelReference(c.getModelReference());
            dto.setColorId(c.getId());
            return dto;
        }).collect(Collectors.toList());
    }
}
