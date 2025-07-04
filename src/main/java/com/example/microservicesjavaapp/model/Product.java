package com.example.microservicesjavaapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a Product entity in the system.
 * This entity is mapped to the 'products' table in the database.
 * Products have a name, price, and category.
 */
@Entity // Marks this class as a JPA entity
@Table(name = "products") // Specifies the table name in the database
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate an all-argument constructor
public class Product {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures the primary key to be auto-generated by the database
    private Long id; // Unique identifier for the product

    @Column(unique = true, nullable = false) // Ensures the product code is unique and not null
    @NotBlank(message = "Product code cannot be empty") // Validation constraint: product code cannot be blank
    private String code; // Unique code for the product

    @Column(nullable = false) // Ensures the product name is not null
    @NotBlank(message = "Product name cannot be empty") // Validation constraint: product name cannot be blank
    private String name; // The name of the product

    @Column(nullable = false) // Ensures the price is not null
    @NotNull(message = "Price cannot be null") // Validation constraint: price cannot be null
    @DecimalMin(value = "0.01", message = "Price must be greater than zero") // Validation constraint: price must be at least 0.01
    private BigDecimal price; // The price of the product

    @Column(nullable = false) // Ensures the category is not null
    @NotBlank(message = "Category cannot be empty") // Validation constraint: category cannot be blank
    private String category; // The category of the product (e.g., "Electronics", "Books", "Food")
}
