package com.nightflow.e2e.scenario;

import com.nightflow.e2e.client.CheckInClient;
import com.nightflow.e2e.config.BaseE2ETest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

/**
 * Check-in Flow E2E Tests
 * 
 * Tests:
 * 1. Preload event tickets to Redis
 * 2. Get event statistics
 * 3. Invalid QR rejection
 * 4. Valid check-in and double check-in prevention
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Check-in Flow E2E Tests")
public class CheckInFlowE2ETest extends BaseE2ETest {
    
    private static CheckInClient checkInClient;
    private static final Long TEST_EVENT_ID = 1L;
    
    // Bu bilet kodu seed-tickets.sql ile veritabanına ekleniyor
    private static final String TEST_TICKET_CODE = "E2E-TEST-CHECKIN-001";
    
    @BeforeAll
    static void initClients() {
        checkInClient = new CheckInClient(baseSpec);
    }
    
    @Test
    @Order(1)
    @DisplayName("1. Preload event tickets to Redis cache")
    void step1_preloadEventTickets() {
        Response response = checkInClient.preloadEventTickets(TEST_EVENT_ID);
        
        response.then()
                .statusCode(200)
                .body("ticketsPreloaded", greaterThanOrEqualTo(0));
        
        System.out.println("✅ Event tickets preloaded to Redis");
    }
    
    @Test
    @Order(2)
    @DisplayName("2. Get event check-in statistics")
    void step2_getEventStats() {
        Response response = checkInClient.getEventStats(TEST_EVENT_ID);
        
        response.then()
                .statusCode(200)
                .body("eventId", equalTo(TEST_EVENT_ID.toString()))
                .body("preloaded", notNullValue());
                
        System.out.println("✅ Event statistics retrieved");
    }
    
    @Test
    @Order(3)
    @DisplayName("3. Invalid ticket code should be rejected")
    void step3_invalidTicketRejected() {
        Response response = checkInClient.validateAndCheckIn("INVALID-TICKET-CODE-12345");
        
        response.then()
                .statusCode(200)
                .body("result", equalTo("INVALID_TICKET"));
        
        System.out.println("✅ Invalid ticket correctly rejected");
    }
    
    @Test
    @Order(4)
    @DisplayName("4. Valid check-in should succeed")
    void step4_validCheckIn() {
        Response response = checkInClient.validateAndCheckIn(TEST_TICKET_CODE);
        
        response.then()
                .statusCode(200)
                .body("result", equalTo("SUCCESS"));
        
        System.out.println("✅ Valid check-in succeeded for ticket: " + TEST_TICKET_CODE);
    }
    
    @Test
    @Order(5)
    @DisplayName("5. Double check-in should be prevented")
    void step5_doubleCheckInPrevented() {
        // Second check-in with same ticket should fail
        Response response = checkInClient.validateAndCheckIn(TEST_TICKET_CODE);
        
        response.then()
                .statusCode(200)
                .body("result", equalTo("ALREADY_USED"));
        
        System.out.println("✅ Double check-in correctly prevented");
    }
}
