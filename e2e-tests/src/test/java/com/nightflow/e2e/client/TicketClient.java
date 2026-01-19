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
    public Response getTicketsByEvent(Long eventId) {
        return given()
                .spec(spec)
                .when()
                .get("/api/tickets/event/" + eventId);
    }
    
    /**
     * Reserve tickets
     */
    public Response reserveTickets(Long ticketId, int quantity, String userId) {
        String body = """
            {
                "ticketId": %d,
                "quantity": %d,
                "userId": "%s"
            }
            """.formatted(ticketId, quantity, userId);
        
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
}
