package com.example.microservicesjavaapp.controller;

import com.example.microservicesjavaapp.dto.AddProductRequest;
import com.example.microservicesjavaapp.dto.CartDto;
import com.example.microservicesjavaapp.dto.CartItemDto;
import com.example.microservicesjavaapp.dto.CreateCartRequest;
import com.example.microservicesjavaapp.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing shopping cart operations.
 * This class exposes endpoints for creating, adding products to, removing products from,
 * listing products in, processing, and listing carts associated with a user.
 * It uses the CartService to perform business logic and returns DTOs.
 */
@RestController // Marks this class as a Spring REST Controller
@RequestMapping("/api/carts") // Base path for all endpoints in this controller
@Validated // Enables validation on controller method parameters
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    /**
     * Creates a new shopping cart for a specified user.
     *
     * @param request The request body containing the user ID.
     * @return ResponseEntity with the created CartDto and HTTP status 201 (Created).
     */
    @PostMapping // Maps POST requests to /api/carts
    public ResponseEntity<CartDto> createCart(@Valid @RequestBody CreateCartRequest request) {
        logger.info("Received request to create cart for user ID: {}", request.getUserId());
        CartDto cartDto = cartService.createCart(request.getUserId());
        return new ResponseEntity<>(cartDto, HttpStatus.CREATED);
    }

    /**
     * Adds a product to an existing shopping cart.
     *
     * @param cartId The ID of the cart to which to add the product.
     * @param request The request body containing product code and quantity.
     * @return ResponseEntity with the updated CartItemDto and HTTP status 200 (OK).
     */
    @PostMapping("/{cartId}/items") // Maps POST requests to /api/carts/{cartId}/items
    public ResponseEntity<CartItemDto> addProductToCart(
            @PathVariable @Min(1) Long cartId,
            @Valid @RequestBody AddProductRequest request) {
        logger.info("Received request to add product {} (qty {}) to cart ID: {}", request.getProductCode(), request.getQuantity(), cartId);
        CartItemDto cartItemDto = cartService.addProductToCart(cartId, request.getProductCode(), request.getQuantity());
        return ResponseEntity.ok(cartItemDto);
    }

    /**
     * Removes a product from an existing shopping cart.
     *
     * @param cartId The ID of the cart from which to remove the product.
     * @param productId The ID of the product to remove.
     * @return ResponseEntity with HTTP status 204 (No Content).
     */
    @DeleteMapping("/{cartId}/items/{productId}") // Maps DELETE requests to /api/carts/{cartId}/items/{productId}
    public ResponseEntity<Void> removeProductFromCart(
            @PathVariable @Min(1) Long cartId,
            @PathVariable @Min(1) Long productId) {
        logger.info("Received request to remove product ID {} from cart ID: {}", productId, cartId);
        cartService.removeProductFromCart(cartId, productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists all products within a specific shopping cart.
     *
     * @param cartId The ID of the cart.
     * @return ResponseEntity with a list of CartItemDto entities and HTTP status 200 (OK).
     */
    @GetMapping("/{cartId}/items") // Maps GET requests to /api/carts/{cartId}/items
    public ResponseEntity<List<CartItemDto>> getCartProducts(@PathVariable @Min(1) Long cartId) {
        logger.info("Received request to get products for cart ID: {}", cartId);
        List<CartItemDto> cartItemDtos = cartService.getCartProducts(cartId);
        return ResponseEntity.ok(cartItemDtos);
    }

    /**
     * Processes a shopping cart order asynchronously.
     * Returns an immediate "processing" message.
     *
     * @param cartId The ID of the cart to process.
     * @return ResponseEntity with a success message and HTTP status 202 (Accepted).
     */
    @PostMapping("/{cartId}/process") // Maps POST requests to /api/carts/{cartId}/process
    public ResponseEntity<String> processCart(@PathVariable @Min(1) Long cartId) {
        logger.info("Received request to process cart ID: {}", cartId);
        cartService.processOrder(cartId); // This call is asynchronous
        return new ResponseEntity<>("Estamos procesando su orden", HttpStatus.ACCEPTED);
    }

    /**
     * Lists all carts associated with a specific user.
     *
     * @param userId The ID of the user.
     * @return ResponseEntity with a list of CartDto entities and HTTP status 200 (OK).
     */
    @GetMapping("/user/{userId}") // Maps GET requests to /api/carts/user/{userId}
    public ResponseEntity<List<CartDto>> getCartsByUserId(@PathVariable @Min(1) Long userId) {
        logger.info("Received request to get carts for user ID: {}", userId);
        List<CartDto> cartDtos = cartService.getCartsByUserId(userId);
        return ResponseEntity.ok(cartDtos);
    }
}