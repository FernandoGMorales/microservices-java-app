package com.example.microservicesjavaapp.repository;

import com.example.microservicesjavaapp.model.Cart;
import com.example.microservicesjavaapp.model.CartItem;
import com.example.microservicesjavaapp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for the CartItem entity.
 * This interface extends JpaRepository, providing standard CRUD operations
 * for the CartItem entity.
 */
@Repository // Marks this interface as a Spring Data JPA repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Finds a CartItem by its associated Cart and Product.
     * This is useful for checking if a product already exists in a cart.
     *
     * @param cart The Cart entity.
     * @param product The Product entity.
     * @return An Optional containing the CartItem if found, or empty if not.
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    /**
     * Finds all CartItems associated with a specific Cart.
     * This is used to retrieve all products within a given shopping cart.
     *
     * @param cart The Cart entity for which to retrieve items.
     * @return A list of CartItem entities.
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * Finds a CartItem by its ID and the associated Cart.
     * This ensures that a cart item belongs to a specific cart before any operation.
     *
     * @param id The ID of the CartItem.
     * @param cart The Cart to which the CartItem belongs.
     * @return An Optional containing the CartItem if found, or empty if not.
     */
    Optional<CartItem> findByIdAndCart(Long id, Cart cart);
}