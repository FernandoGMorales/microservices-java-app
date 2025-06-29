package com.example.microservicesjavaapp.dto;

import com.example.microservicesjavaapp.model.Cart.CartStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Data Transfer Object for Cart entity responses.
 * Used to represent a shopping cart, including user details, status, and items.
 */
@Data // Lombok to generate getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok to generate a no-argument constructor
@AllArgsConstructor // Lombok to generate an all-argument constructor
public class CartDto {
    private Long id;
    private UserDto user; // Nested User DTO
    private LocalDateTime createdAt;
    private CartStatus status;
    private Set<CartItemDto> items; // Nested set of CartItem DTOs
}
