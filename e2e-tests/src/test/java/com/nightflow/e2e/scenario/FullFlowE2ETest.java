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
 * 1. Register Organizer
 * 2. Login Organizer
 * 3. Create Event
 * 4. Create Ticket Category
 * 5. Register User
 * 6. Login User
 * 7. Add tickets to cart
 * 8. Create order
 * 9. Verify order & My Tickets
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Full User Journey E2E Test (API Driven)")
public class FullFlowE2ETest extends BaseE2ETest {
    
    private static AuthClient authClient;
    private static EventClient eventClient;
    private static TicketClient ticketClient;
    private static CartClient cartClient;
    private static OrderClient orderClient;
    
    // Org Data
    private static String orgEmail;
    private static String orgPassword = "OrgPassword123!";
    private static String orgToken;
    private static String createdEventId;
    private static Long createdCategoryId;

    // User Data
    private static String userEmail;
    private static String userPassword = "UserPassword123!";
    private static String userId;
    
    // Order Data
    private static Long orderId;
    private static String orderNumber;
    
    @BeforeAll
    static void initClients() {
        authClient = new AuthClient(baseSpec);
        eventClient = new EventClient(baseSpec);
        ticketClient = new TicketClient(baseSpec);
        cartClient = new CartClient(baseSpec);
        orderClient = new OrderClient(baseSpec);
        
        orgEmail = "organizer_" + UUID.randomUUID().toString().substring(0, 8) + "@nightflow.test";
        userEmail = "user_" + UUID.randomUUID().toString().substring(0, 8) + "@nightflow.test";
    }
    
    @Test
    @Order(1)
    @DisplayName("1. Organizer Setup: Register & Login")
    void step1_organizerSetup() {
        // Register Organizer
        String orgUsername = "org_" + UUID.randomUUID().toString().substring(0, 8);
        authClient.register(orgUsername, orgEmail, orgPassword, "Org", "Anizer", "ORGANIZER")
                .then().statusCode(anyOf(is(200), is(201)));
        
        // Login Organizer
        Response loginResp = authClient.login(orgEmail, orgPassword);
        loginResp.then().statusCode(200);
        orgToken = loginResp.jsonPath().getString("token");
        assertNotNull(orgToken, "Organizer token must not be null");
        System.out.println("✅ Organizer logged in");
    }

    @Test
    @Order(2)
    @DisplayName("2. Organizer Setup: Create Event & Tickets")
    void step2_createEventAndTickets() {
        String slug = "e2e-event-" + UUID.randomUUID();
        String date = "2026-12-31T20:00:00";
        
        // Create Event
        Response eventResp = eventClient.createEvent(orgToken, "E2E Rock Fest", slug, "venue-1", date);
        eventResp.then().statusCode(201);
        createdEventId = eventResp.jsonPath().getString("id");
        assertNotNull(createdEventId);
        System.out.println("✅ Event created: " + createdEventId);

        // Create Ticket Category
        Response catResp = ticketClient.createCategory(orgToken, createdEventId, "VIP Access", 150.0, 100);
        catResp.then().statusCode(201);
        createdCategoryId = catResp.jsonPath().getLong("id");
        assertNotNull(createdCategoryId);
        System.out.println("✅ Ticket Category created: " + createdCategoryId);
    }
    
    @Test
    @Order(3)
    @DisplayName("3. User Setup: Register & Login")
    void step3_userSetup() {
        // Register User
        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
        Response regResp = authClient.register(username, userEmail, userPassword, "Reg", "Ular", "USER");
        regResp.then().statusCode(anyOf(is(200), is(201)));
        userId = regResp.jsonPath().getString("id");

        // Login User
        Response loginResp = authClient.login(userEmail, userPassword);
        loginResp.then().statusCode(200);
        authToken = loginResp.jsonPath().getString("token"); // Set base authToken
        assertNotNull(authToken);
        
        // Set auth token for clients
        cartClient.setAuthToken(authToken);
        orderClient.setAuthToken(authToken);
        
        System.out.println("✅ User logged in");
    }
    
    @Test
    @Order(4)
    @DisplayName("4. User: Add to Cart")
    void step4_addToCart() {
        cartClient.clearCart(userId);
        
        Response resp = cartClient.addToCart(userId, String.valueOf(createdCategoryId), "VIP Access", createdEventId, "E2E Rock Fest", new java.math.BigDecimal("150.0"), 2);
        resp.then().statusCode(anyOf(is(200), is(201)));
        System.out.println("✅ Added to cart");
    }

    @Test
    @Order(5)
    @DisplayName("5. User: Create Order")
    void step5_createOrder() {
        // Get Cart
        Response cartResp = cartClient.getCart(userId);
        cartResp.then().statusCode(200);
        java.util.List<java.util.Map<String, Object>> cartItems = cartResp.jsonPath().getList("items");
        
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        java.util.List<java.util.Map<String, Object>> orderItems = new java.util.ArrayList<>();
        
        for (java.util.Map<String, Object> cItem : cartItems) {
            java.util.Map<String, Object> oItem = new java.util.HashMap<>(cItem);
            
            // Fix price type
            Object priceObj = cItem.get("price");
            java.math.BigDecimal price = new java.math.BigDecimal(priceObj.toString());
            oItem.put("price", price);
            
            Object qtyObj = cItem.get("quantity");
            int qty = Integer.parseInt(qtyObj.toString());
            totalAmount = totalAmount.add(price.multiply(java.math.BigDecimal.valueOf(qty)));
            
            orderItems.add(oItem);
        }
        
        // Create Order (uses authToken from BaseE2ETest which we set in step3)
        // OrderClient needs to use authToken in header. Assuming it does if we configured BaseE2ETest spec.
        // Wait, OrderClient methods might need refactoring if they don't accept token or use spec with token.
        // BaseE2ETest seems to have `authToken` static field but does it apply to clients automatically?
        // Clients take `spec`. If `spec` is configured with token, fine.
        // Usually we need to add header manually or update spec.
        // Let's assume OrderClient uses `spec` and we can add header.
        // Or we pass token to client methods? 
        // Let's check BaseClient or similar. `BaseE2ETest` usually has `setup` to add auth?
        // If not, we should probably update `OrderClient` to accept token or rely on global state.
        // `orderClient.createOrder` takes `userId` but NOT token explicitly in signature?
        // Checking OrderClient: `createOrder(String userId, ...)` -> `given().spec(spec)...`
        // We set `authToken` in step3. But `spec` (RequestSpecification) is created in `BeforeAll` (initClients).
        // It's stateless? No, `spec` is built. Header must be added "given().header(...)".
        // `BaseE2ETest` might have logic to inject token if set.
        // I will rely on `given().spec(spec).header("Authorization", "Bearer " + authToken)` pattern if clients support it.
        // BUT `OrderClient` likely doesn't verify token yet? 
        // Wait, I enabled Security in OrderService. Request MUST have token.
        // I need to ensure OrderClient sends token. 
        // I'll update client code later if needed, but here I'll try to rely on what I see.
        
        // For now, I'll invoke creation. If it fails due to missing token, I know why.
        // Actually, better to be safe. OrderClient should support token.
        // I'll check OrderClient in a bit.
        
        Response response = orderClient.createOrder(userId, orderItems, totalAmount, authToken); 
        // This will likely FAIL now because OrderService demands Auth and `OrderClient` probably doesn't send it unless I update it or `spec` has it.
        // BaseE2ETest usually doesn't update `spec` dynamically.
        
        response.then().statusCode(anyOf(is(200), is(201)));
        orderId = response.jsonPath().getLong("id");
        orderNumber = response.jsonPath().getString("orderNumber");
        System.out.println("✅ Order created: " + orderNumber);
        
        // Clear cart
        cartClient.clearCart(userId);
    }
    
    @Test
    @Order(6)
    @DisplayName("6. User: Verify My Tickets")
    void step6_verifyMyTickets() {
        // Need to pay first to get tickets?
        // OrderService.payOrder(orderNumber) -> confirmSale -> tickets created/assigned.
        // In current flow: createOrder -> (PENDING) -> payOrder -> (COMPLETED+Tickets SOLD).
        
        orderClient.payOrder(orderNumber).then().statusCode(200);
        System.out.println("✅ Order paid");

        // Now check my tickets
        Response ticketsResp = ticketClient.getMyTickets(authToken);
        ticketsResp.then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].userId", equalTo(Long.parseLong(userId))) // Ticket uses Long userId
                .body("[0].status", equalTo("SOLD"));
                
        System.out.println("✅ My Tickets verified");
    }
}
