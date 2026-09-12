package com.labanderaconsulting.eventgateway.transform;

import com.labanderaconsulting.eventgateway.model.EdgeEvent;
import com.labanderaconsulting.eventgateway.model.ProcessedEvent;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Clock;
import java.time.Instant;

/**
 * Pure transformation logic for turning a raw {@link EdgeEvent} into a
 * {@link ProcessedEvent}.
 * <p>
 * Deliberately framework-free: no Camel, no Kafka, no Quarkus context. This
 * is the one piece of business logic in the PoC, and it is unit-testable in
 * isolation, independent of whether it ends up wired behind a Camel route,
 * a plain REST call, or (as it is here) both.
 */
@ApplicationScoped
public class EventTransformer {

    static final String PROCESSED_BY = "event-gateway-poc";

    private final Clock clock;

    public EventTransformer() {
        this(Clock.systemUTC());
    }

    EventTransformer(Clock clock) {
        this.clock = clock;
    }

    /**
     * Transforms a raw event into a processed one. An event missing any of
     * its required fields is marked REJECTED rather than thrown away, so a
     * downstream consumer can see what came in and why it didn't route
     * cleanly.
     */
    public ProcessedEvent transform(EdgeEvent event) {
        String status = event.isValid() ? "PROCESSED" : "REJECTED";
        return new ProcessedEvent(
                event.id(),
                event.source(),
                event.type(),
                event.payload(),
                status,
                PROCESSED_BY,
                Instant.now(clock).toString());
    }
}
