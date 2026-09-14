package com.labanderaconsulting.eventgateway;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class DashboardResourceTest {

    @Test
    void servesAnHtmlPageThatPollsTheEventsEndpoint() {
        given()
                .when().get("/events")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("/events/recent"))
                .body(containsString("live events"));
    }
}
