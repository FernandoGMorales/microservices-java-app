package com.example.microservicesjavaapp.service;

import com.example.microservicesjavaapp.dto.CartDto;
import com.example.microservicesjavaapp.dto.CartItemDto;
import com.example.microservicesjavaapp.dto.ProductDto;
import com.example.microservicesjavaapp.dto.UserDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the CartService class.
 * These tests use Mockito to mock the repository dependencies, allowing us to
 * test the business logic of the service in isolation, now with DTOs.
 */
@ExtendWith(MockitoExtension.class) // Enables Mockito annotations for JUnit 5
public class CartServiceTest {

    @Mock // Mocks the CartRepository dependency
    private CartRepository cartRepository;

    @Mock // Mocks the CartItemRepository dependency
    private CartItemRepository cartItemRepository;

    @Mock // Mocks the ProductRepository dependency
    private ProductRepository productRepository;

    @Mock // Mocks the UserRepository dependency
    private UserRepository userRepository;

    @Mock // Mocks the DiscountRepository dependency
    private DiscountRepository discountRepository;

    @InjectMocks // Injects the mocked dependencies into CartServiceImpl
    private CartServiceImpl cartService; // Inject the implementation class

    private User testUser;
    private Product testProduct1;
    private Product testProduct2;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        // Initialize common test data before each test method
        testUser = new User(1L, "testuser");
        testProduct1 = new Product(101L, "PROD001", "Laptop", new BigDecimal("1200.00"), "Electronics");
        testProduct2 = new Product(102L, "PROD002", "Mouse", new BigDecimal("25.00"), "Electronics");
        // Ensure that the cart's items set is initialized, as the service expects it
        testCart = new Cart(1L, testUser, LocalDateTime.now(), new HashSet<>(), CartStatus.ACTIVE);
    }

    @Test
    void createCart_Success() {
        // Mock the behavior of userRepository.findById to return our test user
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        // Mock the behavior of cartRepository.save to return the saved cart
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Call the service method, expecting a DTO back
        CartDto createdCartDto = cartService.createCart(1L);

        // Assertions on the DTO
        assertNotNull(createdCartDto);
        assertEquals(testCart.getId(), createdCartDto.getId());
        assertEquals(testUser.getId(), createdCartDto.getUser().getId()); // Nested DTO assertion
        assertEquals(testUser.getUsername(), createdCartDto.getUser().getUsername()); // Nested DTO assertion
        assertEquals(CartStatus.ACTIVE, createdCartDto.getStatus());
        // Verify that findById and save methods were called exactly once
        verify(userRepository, times(1)).findById(1L);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void createCart_UserNotFound() {
        // Mock userRepository.findById to return empty, simulating user not found
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Assert that a ResponseStatusException with NOT_FOUND status is thrown
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                cartService.createCart(99L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
        verify(userRepository, times(1)).findById(anyLong());
        // Verify that cartRepository.save was never called
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addProductToCart_NewItem_Success() {
        // Mock repository behaviors
        when(cartRepository.findByIdAndStatus(1L, CartStatus.ACTIVE)).thenReturn(Optional.of(testCart));
        when(productRepository.findByCode("PROD001")).thenReturn(Optional.of(testProduct1));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct1)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            // Simulate saving a new CartItem entity
            CartItem savedItem = invocation.getArgument(0);
            savedItem.setId(1L); // Assign an ID as if saved by JPA
            return savedItem;
        });

        // Call the service method, expecting a DTO back
        CartItemDto addedItemDto = cartService.addProductToCart(1L, "PROD001", 2);

        // Assertions on the DTO
        assertNotNull(addedItemDto);
        assertEquals(1L, addedItemDto.getId());
        assertEquals(testProduct1.getCode(), addedItemDto.getProduct().getCode()); // Nested DTO assertion
        assertEquals(2, addedItemDto.getQuantity());
        // Verify method calls
        verify(cartRepository, times(1)).findByIdAndStatus(1L, CartStatus.ACTIVE);
        verify(productRepository, times(1)).findByCode("PROD001");
        verify(cartItemRepository, times(1)).findByCartAndProduct(testCart, testProduct1);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addProductToCart_UpdateExistingItem_Success() {
        CartItem existingItem = new CartItem(1L, testCart, testProduct1, 1);
        testCart.getItems().add(existingItem); // Add to the cart's collection for internal logic
        // Mock behavior for finding existing item
        when(cartRepository.findByIdAndStatus(1L, CartStatus.ACTIVE)).thenReturn(Optional.of(testCart));
        when(productRepository.findByCode("PROD001")).thenReturn(Optional.of(testProduct1));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct1)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartItemDto updatedItemDto = cartService.addProductToCart(1L, "PROD001", 3);

        assertNotNull(updatedItemDto);
        assertEquals(testProduct1.getCode(), updatedItemDto.getProduct().getCode());
        assertEquals(4, updatedItemDto.getQuantity()); // 1 (existing) + 3 (added) = 4
        verify(cartItemRepository, times(1)).save(existingItem); // Ensure the existing entity was saved
    }

    @Test
    void addProductToCart_CartNotFound() {
        when(cartRepository.findByIdAndStatus(anyLong(), any(CartStatus.class))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                cartService.addProductToCart(99L, "PROD001", 1)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Active cart not found or already processed", exception.getReason());
    }

    @Test
    void addProductToCart_ProductNotFound() {
        when(cartRepository.findByIdAndStatus(1L, CartStatus.ACTIVE)).thenReturn(Optional.of(testCart));
        when(productRepository.findByCode(anyString())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                cartService.addProductToCart(1L, "NONEXISTENT", 1)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Product not found", exception.getReason());
    }

    @Test
    void removeProductFromCart_Success() {
        CartItem existingItem = new CartItem(1L, testCart, testProduct1, 2);
        testCart.getItems().add(existingItem); // Ensure the item is in the cart's collection for removal logic

        when(cartRepository.findByIdAndStatus(1L, CartStatus.ACTIVE)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(101L)).thenReturn(Optional.of(testProduct1));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct1)).thenReturn(Optional.of(existingItem));

        cartService.removeProductFromCart(1L, 101L);

        verify(cartItemRepository, times(1)).delete(existingItem);
        assertFalse(testCart.getItems().contains(existingItem)); // Verify removal from collection
    }

    @Test
    void removeProductFromCart_CartNotFound() {
        when(cartRepository.findByIdAndStatus(anyLong(), any(CartStatus.class))).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                cartService.removeProductFromCart(99L, 101L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Active cart not found or already processed", exception.getReason());
    }

    @Test
    void removeProductFromCart_ProductNotInCart() {
        when(cartRepository.findByIdAndStatus(1L, CartStatus.ACTIVE)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(101L)).thenReturn(Optional.of(testProduct1));
        when(cartItemRepository.findByCartAndProduct(testCart, testProduct1)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                cartService.removeProductFromCart(1L, 101L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Product not found in cart", exception.getReason());
    }

    @Test
    void getCartProducts_Success() {
        CartItem item1 = new CartItem(1L, testCart, testProduct1, 1);
        CartItem item2 = new CartItem(2L, testCart, testProduct2, 3);
        List<CartItem> cartEntities = Arrays.asList(item1, item2); // Entities to be returned by repo

        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCart(testCart)).thenReturn(cartEntities);

        List<CartItemDto> retrievedItems = cartService.getCartProducts(1L);

        assertNotNull(retrievedItems);
        assertEquals(2, retrievedItems.size());
        // Assertions on DTO contents
        assertEquals(item1.getId(), retrievedItems.get(0).getId());
        assertEquals(item1.getProduct().getCode(), retrievedItems.get(0).getProduct().getCode());
        assertEquals(item2.getId(), retrievedItems.get(1).getId());
        assertEquals(item2.getProduct().getCode(), retrievedItems.get(1).getProduct().getCode());

        verify(cartRepository, times(1)).findById(1L);
        verify(cartItemRepository, times(1)).findByCart(testCart);
    }

    @Test
    void getCartProducts_CartNotFound() {
        when(cartRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                cartService.getCartProducts(99L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Cart not found", exception.getReason());
    }

    @Test
    void getCartsByUserId_Success() {
        Cart cart1 = new Cart(1L, testUser, LocalDateTime.now(), new HashSet<>(), CartStatus.ACTIVE);
        Cart cart2 = new Cart(2L, testUser, LocalDateTime.now(), new HashSet<>(), CartStatus.PROCESSED);
        List<Cart> userCartEntities = Arrays.asList(cart1, cart2); // Entities to be returned by repo

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(userCartEntities);

        List<CartDto> retrievedCarts = cartService.getCartsByUserId(1L);

        assertNotNull(retrievedCarts);
        assertEquals(2, retrievedCarts.size());
        // Assertions on DTO contents
        assertEquals(cart1.getId(), retrievedCarts.get(0).getId());
        assertEquals(cart1.getStatus(), retrievedCarts.get(0).getStatus());
        assertEquals(cart2.getId(), retrievedCarts.get(1).getId());
        assertEquals(cart2.getStatus(), retrievedCarts.get(1).getStatus());

        verify(userRepository, times(1)).findById(1L);
        verify(cartRepository, times(1)).findByUser(testUser);
    }

    @Test
    void getCartsByUserId_UserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                cartService.getCartsByUserId(99L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void processOrder_Success_WithDiscount() throws InterruptedException {
        // Setup a cart with items
        CartItem item1 = new CartItem(1L, testCart, testProduct1, 2); // Laptop (Electronics)
        CartItem item2 = new CartItem(2L, testCart, testProduct2, 1); // Mouse (Electronics)
        testCart.getItems().add(item1);
        testCart.getItems().add(item2);

        Discount electronicsDiscount = new Discount(1L, "Electronics", new BigDecimal("10.00")); // 10% discount

        when(cartRepository.findByIdAndStatus(1L, CartStatus.ACTIVE)).thenReturn(Optional.of(testCart));
        when(discountRepository.findByCategory("Electronics")).thenReturn(Optional.of(electronicsDiscount));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Call the async method
        cartService.processOrder(1L);

        // Allow some time for the async method to complete.
        Thread.sleep(2500);

        // Verify that the cart status was updated to PROCESSED
        assertEquals(CartStatus.PROCESSED, testCart.getStatus());
        verify(cartRepository, times(1)).save(testCart);
        verify(cartRepository, times(1)).findByIdAndStatus(1L, CartStatus.ACTIVE);
        verify(discountRepository, times(2)).findByCategory("Electronics"); // Called for each item
    }

    @Test
    void processOrder_CartNotFound_ShouldLogAndNotProceed() throws InterruptedException {
        when(cartRepository.findByIdAndStatus(anyLong(), any(CartStatus.class))).thenReturn(Optional.empty());

        cartService.processOrder(99L);

        Thread.sleep(100);

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void processOrder_EmptyCart() throws InterruptedException {
        // Mock a cart with no items
        testCart.setItems(new HashSet<>());
        when(cartRepository.findByIdAndStatus(1L, CartStatus.ACTIVE)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        cartService.processOrder(1L);

        Thread.sleep(100); // Allow async operation to complete

        assertEquals(CartStatus.PROCESSED, testCart.getStatus());
        verify(cartRepository, times(1)).save(testCart);
    }
}
