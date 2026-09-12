package com.labanderaconsulting.eventgateway;

import com.labanderaconsulting.eventgateway.model.EdgeEvent;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Map;

/**
 * Entry point for the event gateway: accepts an operational event over
 * HTTP and hands it to Kafka for asynchronous processing.
 */
@Path("/ingest")
public class IngestResource {

    private static final Logger LOG = Logger.getLogger(IngestResource.class);

    @Inject
    EventPublisher publisher;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response ingest(EdgeEvent event) {
        if (event == null || !event.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "id, source, and type are required fields"))
                    .build();
        }

        try {
            publisher.publish(event);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish event %s", event.id());
            return Response.serverError()
                    .entity(Map.of("error", "failed to accept event for processing"))
                    .build();
        }

        return Response.accepted(Map.of(
                "id", event.id(),
                "status", "ACCEPTED"
        )).build();
    }
}
