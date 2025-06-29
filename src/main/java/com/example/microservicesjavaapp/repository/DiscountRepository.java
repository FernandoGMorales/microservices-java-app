package com.example.microservicesjavaapp.repository;

import com.example.microservicesjavaapp.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for the Discount entity.
 * This interface extends JpaRepository, providing standard CRUD operations
 * for the Discount entity.
 */
@Repository // Marks this interface as a Spring Data JPA repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    /**
     * Finds a Discount by its associated category.
     * Spring Data JPA automatically generates the query based on the method name.
     *
     * @param category The category for which to find a discount.
     * @return An Optional containing the Discount if found, or empty if not.
     */
    Optional<Discount> findByCategory(String category);
}