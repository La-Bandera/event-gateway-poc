package com.labanderaconsulting.eventgateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labanderaconsulting.eventgateway.model.EdgeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.ProducerTemplate;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Publishes an incoming {@link EdgeEvent} onto the ingestion Kafka topic,
 * where {@link com.labanderaconsulting.eventgateway.route.EdgeEventRoute}
 * picks it up for transformation.
 * <p>
 * Publishing is gated behind {@code gateway.kafka-route.enabled}, the same
 * flag that gates the consumer route, so the REST layer is fully testable
 * (validation, response shape, error handling) without a reachable Kafka
 * broker. End-to-end delivery is verified against the deployed AMQ Streams
 * cluster, not in this unit-level test suite.
 */
@ApplicationScoped
public class EventPublisher {

    @Inject
    ProducerTemplate producerTemplate;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "gateway.topic.in")
    String topicIn;

    @ConfigProperty(name = "camel.component.kafka.brokers")
    String brokers;

    @ConfigProperty(name = "gateway.kafka-route.enabled", defaultValue = "true")
    boolean kafkaRouteEnabled;

    public void publish(EdgeEvent event) throws JsonProcessingException {
        if (!kafkaRouteEnabled) {
            return;
        }
        String json = objectMapper.writeValueAsString(event);
        producerTemplate.sendBody("kafka:" + topicIn + "?brokers=" + brokers, json);
    }
}
