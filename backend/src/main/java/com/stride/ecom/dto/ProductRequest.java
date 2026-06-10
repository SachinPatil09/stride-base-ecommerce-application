package com.stride.ecom.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private Double price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock = 0;

    private String category;
    private String imageUrl;
}
