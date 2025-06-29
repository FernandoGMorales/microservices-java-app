package com.example.microservicesjavaapp.service;

import com.example.microservicesjavaapp.dto.CartDto;
import com.example.microservicesjavaapp.dto.CartItemDto;

import java.util.List;

/**
 * Interface for the shopping cart service.
 * Defines the contract for all cart-related business operations.
 */
public interface CartService {

    /**
     * Creates a new shopping cart for a given user.
     *
     * @param userId The ID of the user for whom to create the cart.
     * @return The newly created CartDto.
     */
    CartDto createCart(Long userId);

    /**
     * Adds a product to an existing cart. If the product already exists in the cart,
     * its quantity is updated. Otherwise, a new cart item is created.
     *
     * @param cartId The ID of the cart to which to add the product.
     * @param productCode The code of the product to add.
     * @param quantity The quantity of the product to add.
     * @return The updated or newly created CartItemDto.
     */
    CartItemDto addProductToCart(Long cartId, String productCode, Integer quantity);

    /**
     * Removes a product from an existing cart.
     *
     * @param cartId The ID of the cart from which to remove the product.
     * @param productId The ID of the product to remove.
     */
    void removeProductFromCart(Long cartId, Long productId);

    /**
     * Retrieves all products (CartItems) within a specific cart.
     *
     * @param cartId The ID of the cart.
     * @return A list of CartItemDto entities.
     */
    List<CartItemDto> getCartProducts(Long cartId);

    /**
     * Retrieves all carts associated with a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of CartDto entities.
     */
    List<CartDto> getCartsByUserId(Long userId);

    /**
     * Asynchronously processes a shopping cart order.
     * This method simulates order validation, discount application, and total calculation.
     * It marks the cart as PROCESSED after completion.
     *
     * @param cartId The ID of the cart to process.
     */
    void processOrder(Long cartId);
}
