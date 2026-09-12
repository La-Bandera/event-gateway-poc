package com.labanderaconsulting.eventgateway;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

/**
 * A human-facing status endpoint distinct from the Kubernetes health probes
 * (see {@code /q/health}) — meant for a person (or a demo audience) checking
 * what topics this instance is wired to.
 */
@Path("/status")
public class StatusResource {

    @ConfigProperty(name = "quarkus.application.name")
    String appName;

    @ConfigProperty(name = "gateway.topic.in")
    String topicIn;

    @ConfigProperty(name = "gateway.topic.out")
    String topicOut;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> status() {
        return Map.of(
                "service", appName,
                "status", "UP",
                "topics", Map.of("in", topicIn, "out", topicOut)
        );
    }
}
