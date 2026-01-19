package com.nightflow.e2e.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * REST client for Auth Service API.
 */
public class AuthClient {
    
    private final RequestSpecification spec;
    
    public AuthClient(RequestSpecification spec) {
        this.spec = spec;
    }
    
    /**
     * Register a new user
     */
    public Response register(String username, String email, String password, String firstName, String lastName) {
        String body = """
            {
                "username": "%s",
                "email": "%s",
                "password": "%s",
                "firstName": "%s",
                "lastName": "%s",
                "role": "USER"
            }
            """.formatted(username, email, password, firstName, lastName);
        
        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/auth/register");
    }
    
    /**
     * Login and get JWT token
     */
    public Response login(String email, String password) {
        String body = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);
        
        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/auth/login");
    }
    
    /**
     * Validate token
     */
    public Response validateToken(String token) {
        return given()
                .spec(spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/auth/validate");
    }
}
