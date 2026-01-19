package com.nightflow.e2e.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * REST client for Check-in Service API.
 */
public class CheckInClient {
    
    private final RequestSpecification spec;
    
    public CheckInClient(RequestSpecification spec) {
        this.spec = spec;
    }
    
    /**
     * Validate and check-in a ticket via QR code
     */
    public Response validateAndCheckIn(String ticketCode) {
        String body = """
            {
                "ticketCode": "%s"
            }
            """.formatted(ticketCode);
        
        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/checkin/validate");
    }
    
    /**
     * Preload tickets into Redis cache for an event
     */
    public Response preloadEventTickets(Long eventId) {
        return given()
                .spec(spec)
                .when()
                .post("/api/checkin/event/" + eventId + "/preload");
    }
    
    /**
     * Get check-in statistics for an event
     */
    public Response getEventStats(Long eventId) {
        return given()
                .spec(spec)
                .when()
                .get("/api/checkin/event/" + eventId + "/stats");
    }
}
