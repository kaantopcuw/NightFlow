package com.nightflow.e2e.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class EventClient {

    private final RequestSpecification spec;

    public EventClient(RequestSpecification spec) {
        this.spec = spec;
    }

    public Response createEvent(String token, String name, String slug, String venueId, String date) {
        String body = """
            {
                "name": "%s",
                "description": "E2E Test Event Desc",
                "slug": "%s",
                "venueId": "%s",
                "venueName": "Test Venue",
                "venueCity": "Istanbul",
                "eventDate": "%s",
                "doorsOpenAt": "%s",
                "category": "CONCERT",
                "minPrice": 100.0,
                "maxPrice": 500.0,
                "posterUrl": "http://example.com/poster.jpg",
                "tags": ["rock", "live"],
                "featured": true
            }
            """.formatted(name, slug, venueId, date, date);

        return given()
                .spec(spec)
                .header("Authorization", "Bearer " + token)
                .body(body)
                .when()
                .post("/api/events");
    }
}
