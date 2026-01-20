package com.nightflow.e2e.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * REST client for Order Service API.
 */
public class OrderClient {
    
    private final RequestSpecification spec;
    private String authToken;
    
    public OrderClient(RequestSpecification spec) {
        this.spec = spec;
    }
    
    /**
     * Set the auth token for subsequent requests
     */
    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    /**
     * Create order
     */
    public Response createOrder(String userId, Object items, java.math.BigDecimal totalAmount) {
        return createOrder(userId, items, totalAmount, null);
    }

    public Response createOrder(String userId, Object items, java.math.BigDecimal totalAmount, String token) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("userId", userId);
        body.put("items", items);
        body.put("totalAmount", totalAmount);

        RequestSpecification s = given().spec(spec).body(body);
        String effectiveToken = token != null ? token : authToken;
        if (effectiveToken != null) {
            s.header("Authorization", "Bearer " + effectiveToken);
        }

        return s.when().post("/api/orders");
    }
    
    /**
     * Pay Order
     */
    public Response payOrder(String orderNumber) {
        RequestSpecification s = given().spec(spec);
        if (authToken != null) {
            s.header("Authorization", "Bearer " + authToken);
        }
        return s.when().post("/api/orders/" + orderNumber + "/pay");
    }

    /**
     * Get order by ID (Order Number UUID)
     */
    public Response getOrder(String orderNumber) {
        RequestSpecification s = given().spec(spec);
        if (authToken != null) {
            s.header("Authorization", "Bearer " + authToken);
        }
        return s.when().get("/api/orders/" + orderNumber);
    }
    
    /**
     * Get orders for user
     */
    public Response getOrdersByUser(String userId) {
        RequestSpecification s = given().spec(spec);
        if (authToken != null) {
            s.header("Authorization", "Bearer " + authToken);
        }
        return s.when().get("/api/orders/user/" + userId);
    }
    
    /**
     * Cancel order
     */
    public Response cancelOrder(Long orderId) {
        RequestSpecification s = given().spec(spec);
        if (authToken != null) {
            s.header("Authorization", "Bearer " + authToken);
        }
        return s.when().post("/api/orders/" + orderId + "/cancel");
    }
}
