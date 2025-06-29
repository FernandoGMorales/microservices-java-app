package com.example.microservicesjavaapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Discount entity responses.
 * Used to expose discount information through the API.
 */
@Data // Lombok to generate getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok to generate a no-argument constructor
@AllArgsConstructor // Lombok to generate an all-argument constructor
public class DiscountDto {
    private Long id;
    private String category;
    private BigDecimal percentage;
}