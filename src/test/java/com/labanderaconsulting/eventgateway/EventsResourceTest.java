package com.labanderaconsulting.eventgateway;

import com.labanderaconsulting.eventgateway.model.EdgeEvent;
import com.labanderaconsulting.eventgateway.model.ProcessedEvent;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

/**
 * Publishing is Kafka-gated and disabled in the test profile, so this
 * exercises the resource directly against the journal it reads from, rather
 * than going through /ingest.
 */
@QuarkusTest
class EventsResourceTest {

    @Inject
    EventJournal journal;

    @Test
    void reportsRecentRawAndProcessedEvents() {
        journal.recordRaw(new EdgeEvent("evt-1", "facility-42", "package.scan", Map.of("zone", "B12")));
        journal.recordProcessed(new ProcessedEvent(
                "evt-1", "facility-42", "package.scan", Map.of("zone", "B12"),
                "PROCESSED", "event-gateway-poc", "2026-09-13T20:00:00Z"));

        given()
                .when().get("/events/recent")
                .then()
                .statusCode(200)
                .body("raw[0].event.id", is("evt-1"))
                .body("processed[0].event.status", is("PROCESSED"));
    }
}
