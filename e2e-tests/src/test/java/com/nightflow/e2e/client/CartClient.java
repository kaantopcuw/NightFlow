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
    
    public CartClient(RequestSpecification spec) {
        this.spec = spec;
    }
    
    /**
     * Get cart for user
     */
    public Response getCart(String userId) {
        return given()
                .spec(spec)
                .when()
                .get("/api/cart/" + userId);
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
        
        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/cart/add");
    }
    
    /**
     * Remove item from cart
     */
    public Response removeFromCart(String userId, Long itemId) {
        return given()
                .spec(spec)
                .when()
                .delete("/api/cart/" + userId + "/items/" + itemId);
    }
    
    /**
     * Clear cart
     */
    public Response clearCart(String userId) {
        return given()
                .spec(spec)
                .when()
                .delete("/api/cart/" + userId);
    }
}
