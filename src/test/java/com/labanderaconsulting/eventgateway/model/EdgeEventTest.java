package com.labanderaconsulting.eventgateway.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EdgeEvent is serialized when published to Kafka and deserialized by
 * EdgeEventRoute when consumed back off it. Those two steps must agree on
 * the wire format -- they didn't: isValid() is a computed convenience
 * method, but Jackson's default bean-property convention picked it up as a
 * "valid" field on serialization, and the record (with no field to bind it
 * to) failed deserialization on the way back in. This locks the round trip.
 */
class EdgeEventTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializationDoesNotLeakTheComputedValidProperty() throws Exception {
        EdgeEvent event = new EdgeEvent("evt-1", "facility-42", "package.scan", Map.of("zone", "B12"));

        String json = mapper.writeValueAsString(event);

        assertThat(json).doesNotContain("\"valid\"");
    }

    @Test
    void roundTripsThroughJsonWithoutError() throws Exception {
        EdgeEvent event = new EdgeEvent("evt-1", "facility-42", "package.scan", Map.of("zone", "B12"));

        String json = mapper.writeValueAsString(event);
        EdgeEvent deserialized = mapper.readValue(json, EdgeEvent.class);

        assertThat(deserialized).isEqualTo(event);
    }
}
