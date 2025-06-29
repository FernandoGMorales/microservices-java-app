package com.example.microservicesjavaapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Product entity responses.
 * Used to expose product details through the API.
 */
@Data // Lombok to generate getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok to generate a no-argument constructor
@AllArgsConstructor // Lombok to generate an all-argument constructor
public class ProductDto {
    private Long id;
    private String code;
    private String name;
    private BigDecimal price;
    private String category;
}

