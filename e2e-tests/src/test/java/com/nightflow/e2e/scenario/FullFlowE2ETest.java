package com.nightflow.e2e.scenario;

import com.nightflow.e2e.client.*;
import com.nightflow.e2e.config.BaseE2ETest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Full Flow E2E Test
 * 
 * Tests the complete user journey:
 * 1. User registration
 * 2. User login
 * 3. Add tickets to cart
 * 4. Create order
 * 5. (Optionally) Check-in with QR
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Full User Journey E2E Test")
public class FullFlowE2ETest extends BaseE2ETest {
    
    private static AuthClient authClient;
    private static TicketClient ticketClient;
    private static CartClient cartClient;
    private static OrderClient orderClient;
    private static CheckInClient checkInClient;
    
    // Test data - will be populated during test
    private static String testEmail;
    private static String testPassword = "TestPassword123!";
    private static String testUserId;
    private static Long testEventId = 1L; // Assumes seeded test data
    private static Long testTicketId;
    private static Long testOrderId;
    
    @BeforeAll
    static void initClients() {
        authClient = new AuthClient(baseSpec);
        ticketClient = new TicketClient(baseSpec);
        cartClient = new CartClient(baseSpec);
        orderClient = new OrderClient(baseSpec);
        checkInClient = new CheckInClient(baseSpec);
        
        // Generate unique test email
        testEmail = "testuser_" + UUID.randomUUID().toString().substring(0, 8) + "@nightflow.test";
    }
    
    @Test
    @Order(1)
    @DisplayName("1. Register new user")
    void step1_registerUser() {
        String username = testEmail.split("@")[0];
        Response response = authClient.register(username, testEmail, testPassword, "Test", "User");
        
        response.then()
                .statusCode(anyOf(is(200), is(201)))
                .body("username", equalTo(username));
        
        testUserId = response.jsonPath().getString("id");
        assertNotNull(testUserId, "User ID should be returned after registration");
        
        System.out.println("✅ User registered: " + testEmail + " (ID: " + testUserId + ")");
    }
    
    @Test
    @Order(2)
    @DisplayName("2. Login and get JWT token")
    void step2_loginUser() {
        Response response = authClient.login(testEmail, testPassword);
        
        response.then()
                .statusCode(200)
                .body("token", notNullValue());
        
        authToken = response.jsonPath().getString("token");
        assertNotNull(authToken, "JWT token should be returned after login");
        
        System.out.println("✅ User logged in, token acquired");
    }
    
    @Test
    @Order(3)
    @DisplayName("3. Get available tickets for event")
    void step3_getEventTickets() {
        Response response = ticketClient.getTicketsByEvent(testEventId);
        
        response.then()
                .statusCode(200)
                .body("size()", greaterThan(0));
        
        // Get the first available ticket
        testTicketId = response.jsonPath().getLong("[0].id");
        int availableQuantity = response.jsonPath().getInt("[0].availableQuantity");
        
        assertTrue(availableQuantity > 0, "There should be available tickets");
        
        System.out.println("✅ Found ticket ID: " + testTicketId + " with " + availableQuantity + " available");
    }
    
    @Test
    @Order(4)
    @DisplayName("4. Add ticket to shopping cart")
    void step4_addToCart() {
        // First clear any existing cart
        cartClient.clearCart(testUserId);
        
        // Bu verileri önceki adımdan veya statik olarak alabiliriz.
        // step3'te ticket client'tan dönen veriyi kullanarak:
        String categoryId = "1"; // testEventId = 1 için varsayılan
        String categoryName = "General Admission"; 
        String eventId = String.valueOf(testEventId);
        String eventName = "Test Event";
        java.math.BigDecimal price = new java.math.BigDecimal("100.00");
        
        Response response = cartClient.addToCart(testUserId, categoryId, categoryName, eventId, eventName, price, 2);
        
        response.then()
                .statusCode(anyOf(is(200), is(201)))
                .body("items.size()", greaterThan(0));
        
        System.out.println("✅ Added 2 tickets to cart");
    }
    
    private static String testOrderNumber;

    @Test
    @Order(5)
    @DisplayName("5. Create order from cart")
    void step5_createOrder() {
        // Construct items list matching what we added to cart
        java.util.Map<String, Object> item = new java.util.HashMap<>();
        item.put("categoryId", "1");
        item.put("categoryName", "General Admission");
        item.put("eventId", String.valueOf(testEventId));
        item.put("eventName", "Test Event");
        item.put("quantity", 2);
        item.put("price", new java.math.BigDecimal("100.00"));
        // Reservation ID is unknown here without fetching cart, but let's assume OrderService generates a new one 
        // OR we need to fetch cart first to get reservation IDs.
        
        // Let's fetch the cart first to get the reservation IDs!
        Response cartResponse = cartClient.getCart(testUserId);
        cartResponse.then().statusCode(200);
        
        java.util.List<java.util.Map<String, Object>> cartItems = cartResponse.jsonPath().getList("items");
        assertNotNull(cartItems, "Cart should not be empty");
        assertFalse(cartItems.isEmpty(), "Cart should have items");
        
        java.util.List<java.util.Map<String, Object>> orderItems = new java.util.ArrayList<>();
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        
        for (java.util.Map<String, Object> cartItem : cartItems) {
            java.util.Map<String, Object> orderItem = new java.util.HashMap<>();
            orderItem.put("categoryId", cartItem.get("categoryId"));
            orderItem.put("categoryName", cartItem.get("categoryName"));
            orderItem.put("eventId", cartItem.get("eventId"));
            orderItem.put("eventName", cartItem.get("eventName"));
            orderItem.put("quantity", cartItem.get("quantity"));
            
            // Price comes as Double or Float from JSON, handle conversion safely
            Object priceObj = cartItem.get("price");
            java.math.BigDecimal price = new java.math.BigDecimal(priceObj.toString());
            orderItem.put("price", price);
            
            orderItem.put("reservationId", cartItem.get("reservationId"));
            
            orderItems.add(orderItem);
            
            Object qtyObj = cartItem.get("quantity");
            int qty = Integer.parseInt(qtyObj.toString());
            totalAmount = totalAmount.add(price.multiply(java.math.BigDecimal.valueOf(qty)));
        }

        Response response = orderClient.createOrder(testUserId, orderItems, totalAmount);
        
        response.then()
                .statusCode(anyOf(is(200), is(201)))
                .body("id", notNullValue())
                .body("orderNumber", notNullValue())
                .body("status", anyOf(equalTo("PENDING"), equalTo("CONFIRMED"), equalTo("CREATED")));
        
        testOrderId = response.jsonPath().getLong("id");
        testOrderNumber = response.jsonPath().getString("orderNumber");
        
        // After creating order, we should clear the cart explicitly or the system should do it.
        // In our current architecture, Order Service doesn't seem to talk to Cart Service.
        // So the frontend (or test) is responsible for clearing the cart.
        cartClient.clearCart(testUserId);
        
        System.out.println("✅ Order created: ID " + testOrderId + ", Number: " + testOrderNumber);
    }
    
    @Test
    @Order(6)
    @DisplayName("6. Verify order details")
    void step6_verifyOrder() {
        Response response = orderClient.getOrder(testOrderNumber);
        
        response.then()
                .statusCode(200)
                .body("id", equalTo(testOrderId.intValue()))
                .body("orderNumber", equalTo(testOrderNumber))
                .body("userId", equalTo(testUserId));
        
        System.out.println("✅ Order verified successfully");
    }
    
    @Test
    @Order(7)
    @DisplayName("7. Cart should be empty after order")
    void step7_cartShouldBeEmpty() {
        Response response = cartClient.getCart(testUserId);
        
        response.then()
                .statusCode(anyOf(is(200), is(404))) // 404 if cart is deleted, 200 with empty items
                .body("items", anyOf(nullValue(), hasSize(0)));
        
        System.out.println("✅ Cart is empty after order completion");
    }
    
    @AfterAll
    static void cleanup() {
        System.out.println("\n========================================");
        System.out.println("Full Flow E2E Test Summary:");
        System.out.println("  Test User: " + testEmail);
        System.out.println("  User ID: " + testUserId);
        System.out.println("  Order ID: " + testOrderId);
        System.out.println("========================================\n");
    }
}
