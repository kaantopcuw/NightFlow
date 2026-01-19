package com.nightflow.e2e.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * REST client for Order Service API.
 */
public class OrderClient {
    
    private final RequestSpecification spec;
    
    public OrderClient(RequestSpecification spec) {
        this.spec = spec;
    }
    
    /**
     * Create order from cart
     */
    /**
     * Create order
     */
    public Response createOrder(String userId, Object items, java.math.BigDecimal totalAmount) {
        // Simple serialization since we don't have a full ObjectMapper here without dependencies
        // In a real scenario, we'd use Jackson. Here we construct a basic JSON.
        // For simplicity in this environment, let's assume the test passes a JSON string for items, 
        // or we use a library if available.
        // Actually, let's change signature to accept the Body directly if complex.
        // But for specific test case:
        
        // Let's rely on RestAssured serialization if we pass a Map/Object.
        // But to avoid class dependency issues, let's create a map structure.
        
         java.util.Map<String, Object> body = new java.util.HashMap<>();
         body.put("userId", userId);
         body.put("items", items);
         body.put("totalAmount", totalAmount);

        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/orders");
    }
    
    /**
     * Get order by ID (Order Number UUID)
     */
    public Response getOrder(String orderNumber) {
        return given()
                .spec(spec)
                .when()
                .get("/api/orders/" + orderNumber);
    }
    
    /**
     * Get orders for user
     */
    public Response getOrdersByUser(String userId) {
        return given()
                .spec(spec)
                .when()
                .get("/api/orders/user/" + userId);
    }
    
    /**
     * Cancel order
     */
    public Response cancelOrder(Long orderId) {
        return given()
                .spec(spec)
                .when()
                .post("/api/orders/" + orderId + "/cancel");
    }
}
