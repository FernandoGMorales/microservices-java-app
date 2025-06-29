package com.example.microservicesjavaapp.repository;

import com.example.microservicesjavaapp.model.Cart;
import com.example.microservicesjavaapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for the Cart entity.
 * This interface extends JpaRepository, providing standard CRUD operations
 * for the Cart entity.
 */
@Repository // Marks this interface as a Spring Data JPA repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Finds a Cart by its ID and ensures it's in an ACTIVE status.
     * This is useful to retrieve carts that are still open for modifications.
     *
     * @param id The ID of the cart to find.
     * @return An Optional containing the active Cart if found, or empty if not.
     */
    Optional<Cart> findByIdAndStatus(Long id, Cart.CartStatus status);

    /**
     * Finds all carts associated with a specific user.
     * This fulfills the requirement to "Listar los carritos asociados a un cliente."
     *
     * @param user The User entity for whom to retrieve carts.
     * @return A list of Carts associated with the given user.
     */
    List<Cart> findByUser(User user);

    /**
     * Finds all carts associated with a specific user and a specific status.
     *
     * @param user The User entity for whom to retrieve carts.
     * @param status The status of the carts to retrieve (e.g., ACTIVE, PROCESSED).
     * @return A list of Carts associated with the given user and status.
     */
    List<Cart> findByUserAndStatus(User user, Cart.CartStatus status);
}
