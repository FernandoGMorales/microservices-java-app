package com.example.microservicesjavaapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for User entity responses.
 * Used to expose specific user information through the API without exposing the full entity.
 */
@Data // Lombok to generate getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok to generate a no-argument constructor
@AllArgsConstructor // Lombok to generate an all-argument constructor
public class UserDto {
    private Long id;
    private String username;
}

