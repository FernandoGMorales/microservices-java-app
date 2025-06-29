package com.example.microservicesjavaapp.repository;

import com.example.microservicesjavaapp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for the Product entity.
 * This interface extends JpaRepository, providing standard CRUD operations
 * for the Product entity.
 */
@Repository // Marks this interface as a Spring Data JPA repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds a Product by its unique code.
     * Spring Data JPA automatically generates the query based on the method name.
     *
     * @param code The unique code of the product to find.
     * @return An Optional containing the Product if found, or empty if not.
     */
    Optional<Product> findByCode(String code);
}