package com.labanderaconsulting.eventgateway.model;

import java.util.Map;

/**
 * An incoming operational event submitted to the gateway for ingestion.
 * <p>
 * Modeled loosely on the kind of event a facility-operations system emits —
 * a package scan, a staffing update, a status change — the pattern this PoC
 * demonstrates: heterogeneous operational events flowing into a single
 * ingestion point, published to Kafka, and picked up by a Camel route for
 * transformation before landing on a downstream topic.
 *
 * @param id      caller-supplied unique identifier for the event
 * @param source  the origin system or facility emitting the event (e.g. "facility-42")
 * @param type    the event category (e.g. "package.scan", "staffing.update")
 * @param payload arbitrary event-specific data
 */
public record EdgeEvent(String id, String source, String type, Map<String, Object> payload) {

    /**
     * @return true if every field required for the event to be routable is present.
     */
    public boolean isValid() {
        return isNonBlank(id) && isNonBlank(source) && isNonBlank(type);
    }

    private static boolean isNonBlank(String value) {
        return value != null && !value.isBlank();
    }
}
