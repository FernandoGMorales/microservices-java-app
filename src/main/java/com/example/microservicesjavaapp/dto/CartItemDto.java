package com.example.microservicesjavaapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for CartItem entity responses.
 * Used to represent a product within a cart, including product details and quantity.
 */
@Data // Lombok to generate getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok to generate a no-argument constructor
@AllArgsConstructor // Lombok to generate an all-argument constructor
public class CartItemDto {
    private Long id;
    private ProductDto product; // Nested Product DTO
    private Integer quantity;
}
