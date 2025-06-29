package com.example.microservicesjavaapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for adding a product to a cart request.
 * Contains the product code and quantity to be added.
 */
@Data // Lombok to generate getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok to generate a no-argument constructor
@AllArgsConstructor // Lombok to generate an all-argument constructor
public class AddProductRequest {
    @NotBlank(message = "Product code cannot be empty")
    private String productCode;
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}