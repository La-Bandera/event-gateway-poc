package com.labanderaconsulting.eventgateway;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class StatusResourceTest {

    @Test
    void reportsServiceAndTopics() {
        given()
                .when().get("/status")
                .then()
                .statusCode(200)
                .body("service", is("event-gateway-poc"))
                .body("status", is("UP"))
                .body("topics.in", is("edge.events.raw"))
                .body("topics.out", is("edge.events.processed"));
    }
}
