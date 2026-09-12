package com.labanderaconsulting.eventgateway;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

/**
 * Exercises the REST layer only: request validation, status codes, response
 * shape. Kafka publishing is disabled in the test profile (see
 * application.properties and EventPublisher), so this suite runs without a
 * broker. End-to-end delivery through Kafka is verified against the deployed
 * AMQ Streams cluster, not here.
 */
@QuarkusTest
class IngestResourceTest {

    @Test
    void acceptsAValidEvent() {
        given()
                .contentType("application/json")
                .body("""
                        {"id":"evt-1","source":"facility-42","type":"package.scan","payload":{"zone":"B12"}}
                        """)
                .when().post("/ingest")
                .then()
                .statusCode(202)
                .body("id", is("evt-1"))
                .body("status", is("ACCEPTED"));
    }

    @Test
    void rejectsAnEventMissingRequiredFields() {
        given()
                .contentType("application/json")
                .body("""
                        {"id":"evt-2","payload":{}}
                        """)
                .when().post("/ingest")
                .then()
                .statusCode(400)
                .body("error", is("id, source, and type are required fields"));
    }

    @Test
    void rejectsAMissingBody() {
        given()
                .contentType("application/json")
                .when().post("/ingest")
                .then()
                .statusCode(400);
    }
}
