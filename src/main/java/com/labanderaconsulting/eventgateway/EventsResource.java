package com.labanderaconsulting.eventgateway;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

/**
 * Backs the live dashboard at {@code /events} — the most recent raw and
 * processed events this instance has actually handled, so a visitor can see
 * the Kafka round-trip happening rather than take it on faith.
 */
@Path("/events/recent")
@Tag(name = "Events", description = "Recent raw and processed events, for the live dashboard")
public class EventsResource {

    @Inject
    EventJournal journal;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Most recent raw and processed events seen by this instance")
    public Map<String, List<?>> recent() {
        return Map.of(
                "raw", journal.recentRaw(),
                "processed", journal.recentProcessed()
        );
    }
}
