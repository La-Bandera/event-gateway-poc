package com.labanderaconsulting.eventgateway.model;

import java.util.Map;

/**
 * The result of running an {@link EdgeEvent} through the transformation route:
 * the original fields, plus provenance about when and how it was processed.
 *
 * @param id           the original event id
 * @param source       the original event source
 * @param type         the original event type
 * @param payload      the original event payload, unmodified
 * @param status       the outcome of processing ("PROCESSED" or "REJECTED")
 * @param processedBy  the identifier of the service that processed the event
 * @param processedAt  ISO-8601 timestamp of when the event was processed
 */
public record ProcessedEvent(
        String id,
        String source,
        String type,
        Map<String, Object> payload,
        String status,
        String processedBy,
        String processedAt) {
}
