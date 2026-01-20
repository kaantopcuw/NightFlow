package com.nightflow.e2e.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * REST client for Ticket Service API.
 */
public class TicketClient {
    
    private final RequestSpecification spec;
    
    public TicketClient(RequestSpecification spec) {
        this.spec = spec;
    }
    
    /**
     * Get tickets for an event
     */
    /**
     * Create Ticket Category (Organizer only)
     */
    public Response createCategory(String token, String eventId, String name, double price, int quantity) {
        String body = """
            {
                "eventId": "%s",
                "name": "%s",
                "description": "Test Category",
                "price": %s,
                "totalQuantity": %d,
                "salesStartAt": "2030-01-01T00:00:00",
                "salesEndAt": "2030-12-31T23:59:59"
            }
            """.formatted(eventId, name, price, quantity);

        return given()
                .spec(spec)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post("/api/ticket-categories");
    }

    /**
     * Get tickets for an event
     */
    public Response getTicketsByEvent(Long eventId) {
        return given()
                .spec(spec)
                .when()
                .get("/api/tickets/event/" + eventId);
    }
    
    /**
     * Reserve tickets
     */
    public Response reserveTickets(Long categoryId, int quantity, String sessionId) {
        String body = """
            {
                "categoryId": %d,
                "quantity": %d,
                "sessionId": "%s"
            }
            """.formatted(categoryId, quantity, sessionId);
        
        return given()
                .spec(spec)
                .body(body)
                .when()
                .post("/api/tickets/reserve");
    }
    
    /**
     * Confirm reservation
     */
    public Response confirmReservation(String reservationId) {
        return given()
                .spec(spec)
                .when()
                .post("/api/tickets/confirm/" + reservationId);
    }
    
    /**
     * Cancel reservation
     */
    public Response cancelReservation(String reservationId) {
        return given()
                .spec(spec)
                .when()
                .post("/api/tickets/cancel/" + reservationId);
    }
    
    /**
     * Get My Tickets
     */
    public Response getMyTickets(String token) {
        return given()
                .spec(spec)
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/tickets/my-tickets");
    }
}
