package com.stride.ecom.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddToCartRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
