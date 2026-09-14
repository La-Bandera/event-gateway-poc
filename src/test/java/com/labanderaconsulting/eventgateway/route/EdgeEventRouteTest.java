package com.labanderaconsulting.eventgateway.route;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every other test in this suite runs with {@code gateway.kafka-route.enabled=false}
 * (the test-profile default, so the REST layer is testable without a broker) --
 * which means none of them would ever notice if this route silently stopped
 * registering. It did, once: {@link EdgeEventRoute} was missing a CDI scope
 * annotation, so Quarkus's default annotation-based bean discovery never
 * instantiated it, and Camel Quarkus never saw it to add its route. No
 * error, no test failure -- just "Routes startup (total:0)" at boot and a
 * Kafka consumer that never existed. This test overrides the route flag back
 * on and asserts the route is actually there.
 */
@QuarkusTest
@TestProfile(EdgeEventRouteTest.RouteEnabledProfile.class)
class EdgeEventRouteTest {

    @Inject
    CamelContext camelContext;

    @Test
    void theRouteIsRegisteredWhenEnabled() {
        assertThat(camelContext.getRoutes()).hasSize(1);
        assertThat(camelContext.getRoute("edge-event-transform")).isNotNull();
    }

    public static class RouteEnabledProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            // A broker that will never answer -- this test only asserts the
            // route is registered, not that it can actually connect.
            return Map.of(
                    "gateway.kafka-route.enabled", "true",
                    "camel.component.kafka.brokers", "localhost:1"
            );
        }
    }
}
