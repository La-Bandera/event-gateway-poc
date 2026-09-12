package com.labanderaconsulting.eventgateway.transform;

import com.labanderaconsulting.eventgateway.model.EdgeEvent;
import com.labanderaconsulting.eventgateway.model.ProcessedEvent;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain JUnit test — no Quarkus context, no Camel, no Kafka. Runs instantly
 * anywhere the JVM does, which is the point: the one piece of real business
 * logic in this PoC shouldn't need infrastructure to verify.
 */
class EventTransformerTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-09-14T12:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    private final EventTransformer transformer = new EventTransformer(FIXED_CLOCK);

    @Test
    void transformsAValidEventToProcessed() {
        EdgeEvent event = new EdgeEvent("evt-1", "facility-42", "package.scan", Map.of("zone", "B12"));

        ProcessedEvent result = transformer.transform(event);

        assertThat(result.id()).isEqualTo("evt-1");
        assertThat(result.source()).isEqualTo("facility-42");
        assertThat(result.type()).isEqualTo("package.scan");
        assertThat(result.payload()).containsEntry("zone", "B12");
        assertThat(result.status()).isEqualTo("PROCESSED");
        assertThat(result.processedBy()).isEqualTo("event-gateway-poc");
        assertThat(result.processedAt()).isEqualTo(FIXED_INSTANT.toString());
    }

    @Test
    void marksAnEventMissingRequiredFieldsAsRejected() {
        EdgeEvent event = new EdgeEvent("evt-2", "", "package.scan", Map.of());

        ProcessedEvent result = transformer.transform(event);

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.id()).isEqualTo("evt-2");
    }

    @Test
    void preservesAnEmptyPayloadRatherThanFailing() {
        EdgeEvent event = new EdgeEvent("evt-3", "facility-7", "staffing.update", Map.of());

        ProcessedEvent result = transformer.transform(event);

        assertThat(result.status()).isEqualTo("PROCESSED");
        assertThat(result.payload()).isEmpty();
    }
}
