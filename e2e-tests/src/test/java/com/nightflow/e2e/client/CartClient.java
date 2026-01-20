package com.nightflow.e2e.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.math.BigDecimal;

import static io.restassured.RestAssured.given;

/**
 * REST client for Shopping Cart Service API.
 */
public class CartClient {
    
    private final RequestSpecification spec;
    private String authToken;
    
    public CartClient(RequestSpecification spec) {
        this.spec = spec;
    }
    
    /**
     * Set the auth token for subsequent requests
     */
    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    /**
     * Get cart for user
     */
    public Response getCart(String userId) {
        var request = given().spec(spec);
        if (authToken != null) {
            request.header("Authorization", "Bearer " + authToken);
        }
        return request.when().get("/api/cart/" + userId);
    }
    
    /**
     * Add item to cart
     */
    public Response addToCart(String sessionId, String categoryId, String categoryName, String eventId, String eventName, BigDecimal price, int quantity) {
        String body = """
            {
                "categoryId": "%s",
                "categoryName": "%s",
                "eventId": "%s",
                "eventName": "%s",
                "price": %s,
                "quantity": %d,
                "sessionId": "%s"
            }
            """.formatted(categoryId, categoryName, eventId, eventName, price, quantity, sessionId);
        
        var request = given().spec(spec);
        if (authToken != null) {
            request.header("Authorization", "Bearer " + authToken);
        }
        return request.body(body).when().post("/api/cart/add");
    }
    
    /**
     * Remove item from cart
     */
    public Response removeFromCart(String userId, Long itemId) {
        var request = given().spec(spec);
        if (authToken != null) {
            request.header("Authorization", "Bearer " + authToken);
        }
        return request.when().delete("/api/cart/" + userId + "/items/" + itemId);
    }
    
    /**
     * Clear cart
     */
    public Response clearCart(String userId) {
        var request = given().spec(spec);
        if (authToken != null) {
            request.header("Authorization", "Bearer " + authToken);
        }
        return request.when().delete("/api/cart/" + userId);
    }
}
