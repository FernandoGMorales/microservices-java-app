package com.example.microservicesjavaapp.service;

import com.example.microservicesjavaapp.model.Cart;
import com.example.microservicesjavaapp.model.Cart.CartStatus;
import com.example.microservicesjavaapp.model.CartItem;
import com.example.microservicesjavaapp.model.Discount;
import com.example.microservicesjavaapp.model.Product;
import com.example.microservicesjavaapp.model.User;
import com.example.microservicesjavaapp.repository.CartItemRepository;
import com.example.microservicesjavaapp.repository.CartRepository;
import com.example.microservicesjavaapp.repository.DiscountRepository;
import com.example.microservicesjavaapp.repository.ProductRepository;
import com.example.microservicesjavaapp.repository.UserRepository;
import com.example.microservicesjavaapp.dto.CartDto;
import com.example.microservicesjavaapp.dto.CartItemDto;
import com.example.microservicesjavaapp.dto.ProductDto;
import com.example.microservicesjavaapp.dto.UserDto;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Service implementation for managing shopping cart operations.
 * This class handles the business logic related to creating, adding, removing,
 * listing, and processing carts, as well as applying discounts.
 * It also manages concurrency to prevent race conditions and maps entities to DTOs.
 */
@Service // Marks this class as a Spring service
public class CartServiceImpl implements CartService { // Implements the new interface

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiscountRepository discountRepository;

    // A map to hold locks for each cart, preventing race conditions
    private final ConcurrentHashMap<Long, Lock> cartLocks = new ConcurrentHashMap<>();

    @Override // Mark that this method implements an interface method
    @Transactional
    public CartDto createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User with ID {} not found.", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);
        cart = cartRepository.save(cart);
        logger.info("Cart created with ID {} for user ID {}.", cart.getId(), userId);
        return toCartDto(cart);
    }

    @Override
    @Transactional
    public CartItemDto addProductToCart(Long cartId, String productCode, Integer quantity) {
        Lock lock = cartLocks.computeIfAbsent(cartId, k -> new ReentrantLock());
        lock.lock();

        try {
            Cart cart = cartRepository.findByIdAndStatus(cartId, CartStatus.ACTIVE)
                    .orElseThrow(() -> {
                        logger.warn("Active cart with ID {} not found.", cartId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Active cart not found or already processed");
                    });

            Product product = productRepository.findByCode(productCode)
                    .orElseThrow(() -> {
                        logger.warn("Product with code {} not found.", productCode);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
                    });

            Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProduct(cart, product);

            CartItem cartItem;
            if (existingCartItem.isPresent()) {
                cartItem = existingCartItem.get();
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                logger.info("Updated quantity of product {} in cart {}. New quantity: {}", productCode, cartId, cartItem.getQuantity());
            } else {
                cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProduct(product);
                cartItem.setQuantity(quantity);
                cart.getItems().add(cartItem);
                logger.info("Added product {} to cart {} with quantity {}.", productCode, cartId, quantity);
            }
            return toCartItemDto(cartItemRepository.save(cartItem));
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional
    public void removeProductFromCart(Long cartId, Long productId) {
        Lock lock = cartLocks.computeIfAbsent(cartId, k -> new ReentrantLock());
        lock.lock();

        try {
            Cart cart = cartRepository.findByIdAndStatus(cartId, CartStatus.ACTIVE)
                    .orElseThrow(() -> {
                        logger.warn("Active cart with ID {} not found.", cartId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Active cart not found or already processed");
                    });

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        logger.warn("Product with ID {} not found.", productId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
                    });

            CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                    .orElseThrow(() -> {
                        logger.warn("Product with ID {} not found in cart {}.", productId, cartId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in cart");
                    });

            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
            logger.info("Removed product with ID {} from cart {}.", productId, cartId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<CartItemDto> getCartProducts(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    logger.warn("Cart with ID {} not found.", cartId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found");
                });
        return cartItemRepository.findByCart(cart).stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CartDto> getCartsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User with ID {} not found.", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
        return cartRepository.findByUser(user).stream()
                .map(this::toCartDto)
                .collect(Collectors.toList());
    }

    @Override
    @Async
    @Transactional
    public void processOrder(Long cartId) {
        Lock lock = cartLocks.computeIfAbsent(cartId, k -> new ReentrantLock());
        lock.lock();

        try {
            logger.info("Starting asynchronous processing for cart ID {}.", cartId);

            Cart cart = cartRepository.findByIdAndStatus(cartId, CartStatus.ACTIVE)
                    .orElseThrow(() -> {
                        logger.warn("Active cart with ID {} not found for processing.", cartId);
                        throw new IllegalStateException("Cart not found or already processed for ID: " + cartId);
                    });

            BigDecimal totalAmount = BigDecimal.ZERO;
            Set<CartItem> items = cart.getItems();

            if (items.isEmpty()) {
                logger.warn("Cart {} is empty, cannot process.", cartId);
                cart.setStatus(CartStatus.PROCESSED);
                cartRepository.save(cart);
                logger.info("Cart {} processed (empty).", cartId);
                return;
            }

            for (CartItem item : items) {
                Product product = item.getProduct();
                BigDecimal itemPrice = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                logger.debug("Item: {}, Quantity: {}, Price: {}", product.getName(), item.getQuantity(), itemPrice);

                Optional<Discount> discount = discountRepository.findByCategory(product.getCategory());
                if (discount.isPresent()) {
                    BigDecimal discountAmount = itemPrice.multiply(discount.get().getPercentage().divide(BigDecimal.valueOf(100)));
                    itemPrice = itemPrice.subtract(discountAmount);
                    logger.debug("Applied {}% discount to {}. Discount amount: {}. New item price: {}",
                            discount.get().getPercentage(), product.getName(), discountAmount, itemPrice);
                }
                totalAmount = totalAmount.add(itemPrice);
            }

            Thread.sleep(2000);

            cart.setStatus(CartStatus.PROCESSED);
            cartRepository.save(cart);

            logger.info("Order for cart ID {} processed successfully. Total amount: {}", cartId, totalAmount);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Order processing for cart ID {} was interrupted.", cartId, e);
        } catch (Exception e) {
            logger.error("Error processing order for cart ID {}: {}", cartId, e.getMessage(), e);
        } finally {
            cartLocks.remove(cartId);
            lock.unlock();
        }
    }

    /**
     * Helper method to convert a User entity to a UserDto.
     * @param user The User entity.
     * @return The corresponding UserDto.
     */
    private UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getUsername());
    }

    /**
     * Helper method to convert a Product entity to a ProductDto.
     * @param product The Product entity.
     * @return The corresponding ProductDto.
     */
    private ProductDto toProductDto(Product product) {
        return new ProductDto(product.getId(), product.getCode(), product.getName(), product.getPrice(), product.getCategory());
    }

    /**
     * Helper method to convert a CartItem entity to a CartItemDto.
     * @param cartItem The CartItem entity.
     * @return The corresponding CartItemDto.
     */
    private CartItemDto toCartItemDto(CartItem cartItem) {
        return new CartItemDto(cartItem.getId(), toProductDto(cartItem.getProduct()), cartItem.getQuantity());
    }

    /**
     * Helper method to convert a Cart entity to a CartDto.
     * Includes nested DTO conversion for User and CartItems.
     * @param cart The Cart entity.
     * @return The corresponding CartDto.
     */
    private CartDto toCartDto(Cart cart) {
        Set<CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toSet());
        return new CartDto(cart.getId(), toUserDto(cart.getUser()), cart.getCreatedAt(), cart.getStatus(), itemDtos);
    }
}
