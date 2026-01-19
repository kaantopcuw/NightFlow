package com.nightflow.e2e.config;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base configuration for all E2E tests.
 * Sets up RestAssured with gateway base URL.
 */
public abstract class BaseE2ETest {

    protected static final String GATEWAY_URL = System.getProperty("gateway.url", "http://localhost:8080");
    
    protected static RequestSpecification baseSpec;
    protected static String authToken;

    @BeforeAll
    static void setupRestAssured() {
        RestAssured.baseURI = GATEWAY_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);
        
        baseSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.URI)
                .build();
    }
    
    /**
     * Helper to create authenticated request spec with Bearer token
     */
    protected RequestSpecification authenticatedSpec() {
        return new RequestSpecBuilder()
                .addRequestSpecification(baseSpec)
                .addHeader("Authorization", "Bearer " + authToken)
                .build();
    }
}
