package com.labanderaconsulting.eventgateway;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * Trivial liveness probe for OpenShift — the process being able to answer
 * at all is sufficient for liveness (readiness for actual Kafka connectivity
 * is a separate concern this PoC doesn't need to model).
 */
@Liveness
public class GatewayLivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("event-gateway-poc is alive");
    }
}
