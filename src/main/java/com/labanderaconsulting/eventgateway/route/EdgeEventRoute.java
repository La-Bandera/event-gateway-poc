package com.labanderaconsulting.eventgateway.route;

import com.labanderaconsulting.eventgateway.EventJournal;
import com.labanderaconsulting.eventgateway.model.EdgeEvent;
import com.labanderaconsulting.eventgateway.model.ProcessedEvent;
import com.labanderaconsulting.eventgateway.transform.EventTransformer;
import jakarta.inject.Inject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * The gateway's core integration route: consume a raw event from Kafka,
 * transform it, publish the result to a downstream Kafka topic.
 * <p>
 * This is the framework-agnostic heart of the PoC — the pattern is the same
 * one used to bridge legacy message-queue traffic (e.g. IBM WebSphere MQ)
 * onto a modern event backbone: consume, transform, republish, with the
 * transformation logic kept independent of the messaging technology.
 * <p>
 * Route registration is gated behind {@code gateway.kafka-route.enabled} so
 * unit/component tests can boot the application context without requiring a
 * reachable Kafka broker; it is {@code true} by default and disabled only in
 * the {@code test} profile (see {@code application.properties}).
 */
public class EdgeEventRoute extends RouteBuilder {

    @Inject
    EventTransformer transformer;

    @Inject
    EventJournal journal;

    @ConfigProperty(name = "gateway.topic.in")
    String topicIn;

    @ConfigProperty(name = "gateway.topic.out")
    String topicOut;

    @ConfigProperty(name = "gateway.kafka-route.enabled", defaultValue = "true")
    boolean kafkaRouteEnabled;

    @Override
    public void configure() {
        if (!kafkaRouteEnabled) {
            return;
        }

        onException(Exception.class)
                .routeId("edge-event-error-handler")
                .handled(true)
                .log("Failed to process event on ${routeId}: ${exception.message}");

        from("kafka:" + topicIn + "?brokers={{camel.component.kafka.brokers}}")
                .routeId("edge-event-transform")
                .log("Received raw event on " + topicIn + ": ${body}")
                .unmarshal().json(JsonLibrary.Jackson, EdgeEvent.class)
                .process(exchange -> {
                    EdgeEvent raw = exchange.getIn().getBody(EdgeEvent.class);
                    ProcessedEvent processed = transformer.transform(raw);
                    journal.recordProcessed(processed);
                    exchange.getIn().setBody(processed);
                })
                .marshal().json(JsonLibrary.Jackson)
                .log("Publishing processed event to " + topicOut + ": ${body}")
                .to("kafka:" + topicOut + "?brokers={{camel.component.kafka.brokers}}");
    }
}
