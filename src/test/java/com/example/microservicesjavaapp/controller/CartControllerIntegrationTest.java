package com.example.microservicesjavaapp.controller;

import com.example.microservicesjavaapp.model.Cart;
import com.example.microservicesjavaapp.model.Cart.CartStatus;
import com.example.microservicesjavaapp.model.CartItem;
import com.example.microservicesjavaapp.model.User;
import com.example.microservicesjavaapp.model.Product;
import com.example.microservicesjavaapp.model.Discount;
import com.example.microservicesjavaapp.repository.CartRepository;
import com.example.microservicesjavaapp.repository.UserRepository;
import com.example.microservicesjavaapp.repository.ProductRepository;
import com.example.microservicesjavaapp.repository.DiscountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Integration tests for the CartController.
 * These tests use MockMvc to send HTTP requests and verify the responses,
 * interacting with the full Spring application context and the H2 in-memory database.
 * Basic authentication is applied to all requests as per the SecurityConfig.
 */
@SpringBootTest // Boots up the full Spring application context
@AutoConfigureMockMvc // Configures MockMvc for testing MVC controllers
@ActiveProfiles("test") // Use a 'test' profile if specific test configs are needed (optional)
public class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Used to perform HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // Used to convert objects to JSON and vice-versa

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private DiscountRepository discountRepository;

    private User user1;
    private User user2;
    private Product product1;
    private Product product2;
    private Discount electronicsDiscount;

    // Credentials for basic auth
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    @BeforeEach
    void setup() {
        // Clear all repositories before each test to ensure a clean state
        cartRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();
        discountRepository.deleteAll();

        // Populate initial data for tests
        user1 = userRepository.save(new User(null, "testuser1"));
        user2 = userRepository.save(new User(null, "testuser2"));

        product1 = productRepository.save(new Product(null, "PROD001", "Laptop", new BigDecimal("1200.00"), "Electronics"));
        product2 = productRepository.save(new Product(null, "PROD002", "Mouse", new BigDecimal("25.00"), "Electronics"));
        productRepository.save(new Product(null, "PROD003", "Book", new BigDecimal("50.00"), "Books"));

        electronicsDiscount = discountRepository.save(new Discount(null, "Electronics", new BigDecimal("10.00"))); // 10% discount
    }

    @Test
    void createCart_Success() throws Exception {
        // Using the new CreateCartRequest DTO structure
        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("userId", user1.getId());

        mockMvc.perform(post("/api/carts")
                        .with(httpBasic(USERNAME, PASSWORD)) // Apply basic authentication
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated()) // Expect HTTP 201 Created
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.user.id", is(user1.getId().intValue()))) // Asserting on nested UserDto
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void createCart_UserNotFound_ReturnsNotFound() throws Exception {
        Map<String, Long> requestBody = new HashMap<>();
        requestBody.put("userId", 999L); // Non-existent user ID

        mockMvc.perform(post("/api/carts")
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound()) // Expect HTTP 404 Not Found
                .andExpect(jsonPath("$.message", is("User not found")));
    }

    @Test
    void addProductToCart_NewItem_Success() throws Exception {
        Cart cart = cartRepository.save(new Cart(null, user1, LocalDateTime.now(), null, CartStatus.ACTIVE));

        // Using the new AddProductRequest DTO structure
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productCode", product1.getCode());
        requestBody.put("quantity", 2);

        mockMvc.perform(post("/api/carts/{cartId}/items", cart.getId())
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.product.code", is(product1.getCode()))) // Asserting on nested ProductDto
                .andExpect(jsonPath("$.quantity", is(2)));
    }

    @Test
    void addProductToCart_UpdateExistingItem_Success() throws Exception {
        Cart cart = cartRepository.save(new Cart(null, user1, LocalDateTime.now(), new HashSet<>(), CartStatus.ACTIVE));
        cart.getItems().add(new CartItem(null, cart, product1, 1));
        cartRepository.save(cart); // Save with existing item

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productCode", product1.getCode());
        requestBody.put("quantity", 3);

        mockMvc.perform(post("/api/carts/{cartId}/items", cart.getId())
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(4))); // 1 (existing) + 3 (added) = 4
    }

    @Test
    void addProductToCart_CartNotFound_ReturnsNotFound() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productCode", product1.getCode());
        requestBody.put("quantity", 1);

        mockMvc.perform(post("/api/carts/{cartId}/items", 999L) // Non-existent cart ID
                        .with(httpBasic(USERNAME, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Active cart not found or already processed")));
    }

    @Test
    void removeProductFromCart_Success() throws Exception {
        Cart cart = cartRepository.save(new Cart(null, user1, LocalDateTime.now(), new HashSet<>(), CartStatus.ACTIVE));
        CartItem cartItem = new CartItem(null, cart, product1, 2);
        cart.getItems().add(cartItem);
        cartRepository.save(cart); // Ensure cart has the item

        mockMvc.perform(delete("/api/carts/{cartId}/items/{productId}", cart.getId(), product1.getId())
                        .with(httpBasic(USERNAME, PASSWORD)))
                .andExpect(status().isNoContent()); // Expect HTTP 204 No Content
    }

    @Test
    void removeProductFromCart_ProductNotInCart_ReturnsNotFound() throws Exception {
        Cart cart = cartRepository.save(new Cart(null, user1, LocalDateTime.now(), null, CartStatus.ACTIVE));

        mockMvc.perform(delete("/api/carts/{cartId}/items/{productId}", cart.getId(), product1.getId())
                        .with(httpBasic(USERNAME, PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Product not found in cart")));
    }

    @Test
    void getCartProducts_Success() throws Exception {
        Cart cart = cartRepository.save(new Cart(null, user1, LocalDateTime.now(), new HashSet<>(), CartStatus.ACTIVE));
        cart.getItems().add(new CartItem(null, cart, product1, 2));
        cart.getItems().add(new CartItem(null, cart, product2, 1));
        cartRepository.save(cart);

        mockMvc.perform(get("/api/carts/{cartId}/items", cart.getId())
                        .with(httpBasic(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].product.id", Matchers.containsInAnyOrder(
                        product1.getId().intValue(),
                        product2.getId().intValue()
                ))); // Asserting on nested ProductDto
    }

    @Test
    void getCartProducts_CartNotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/carts/{cartId}/items", 999L)
                        .with(httpBasic(USERNAME, PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Cart not found")));
    }

    @Test
    void processCart_Success() throws Exception {
        Cart cart = cartRepository.save(new Cart(null, user1, LocalDateTime.now(), new HashSet<>(), CartStatus.ACTIVE));
        cart.getItems().add(new CartItem(null, cart, product1, 1)); // Add an item to make processing meaningful
        cartRepository.save(cart);

        mockMvc.perform(post("/api/carts/{cartId}/process", cart.getId())
                        .with(httpBasic(USERNAME, PASSWORD)))
                .andExpect(status().isAccepted()) // Expect HTTP 202 Accepted
                .andExpect(content().string("Estamos procesando su orden"));

        // Verify cart status changed to PROCESSED after some delay (async operation)
        Thread.sleep(3000); // Wait longer than the simulated delay in CartService
        Cart processedCart = cartRepository.findById(cart.getId()).orElseThrow();
        assertThat(processedCart.getStatus(), is(CartStatus.PROCESSED));
    }

    @Test
    void getCartsByUserId_Success() throws Exception {
        Cart cart1 = cartRepository.save(new Cart(null, user1, LocalDateTime.now(), null, CartStatus.ACTIVE));
        Cart cart2 = cartRepository.save(new Cart(null, user1, LocalDateTime.now(), null, CartStatus.PROCESSED));
        cartRepository.save(new Cart(null, user2, LocalDateTime.now(), null, CartStatus.ACTIVE)); // Cart for another user

        mockMvc.perform(get("/api/carts/user/{userId}", user1.getId())
                        .with(httpBasic(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].user.id", is(user1.getId().intValue()))) // Asserting on nested UserDto
                .andExpect(jsonPath("$[1].user.id", is(user1.getId().intValue())));
    }

    @Test
    void getCartsByUserId_UserNotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/carts/user/{userId}", 999L)
                        .with(httpBasic(USERNAME, PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("User not found")));
    }

    @Test
    void accessWithoutAuthentication_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/carts/user/1"))
                .andExpect(status().isUnauthorized()); // Expect HTTP 401 Unauthorized
    }

    @Test
    void accessWithInvalidAuthentication_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/carts/user/1")
                        .with(httpBasic("wronguser", "wrongpass")))
                .andExpect(status().isUnauthorized());
    }
}
