package com.labanderaconsulting.eventgateway;

import com.labanderaconsulting.eventgateway.model.EdgeEvent;
import com.labanderaconsulting.eventgateway.model.ProcessedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In-memory record of the most recent events flowing through the gateway,
 * for the live dashboard at {@code /events}.
 * <p>
 * This is intentionally not backed by Kafka itself (no consumer group
 * reading the topics back) — it's a bounded log of what this instance has
 * personally seen, populated at the two points that already touch every
 * event: {@link EventPublisher} on ingest, {@link
 * com.labanderaconsulting.eventgateway.route.EdgeEventRoute} on completion.
 * Not persisted; a pod restart clears it, which is fine for a PoC dashboard.
 */
@ApplicationScoped
public class EventJournal {

    /** Timestamped snapshot of an event as it passed through the gateway. */
    public record Entry<T>(Instant at, T event) {
    }

    @ConfigProperty(name = "gateway.dashboard.buffer-size", defaultValue = "50")
    int bufferSize;

    private final Deque<Entry<EdgeEvent>> raw = new ArrayDeque<>();
    private final Deque<Entry<ProcessedEvent>> processed = new ArrayDeque<>();

    public synchronized void recordRaw(EdgeEvent event) {
        addBounded(raw, new Entry<>(Instant.now(), event));
    }

    public synchronized void recordProcessed(ProcessedEvent event) {
        addBounded(processed, new Entry<>(Instant.now(), event));
    }

    public synchronized List<Entry<EdgeEvent>> recentRaw() {
        return raw.stream().collect(Collectors.toList());
    }

    public synchronized List<Entry<ProcessedEvent>> recentProcessed() {
        return processed.stream().collect(Collectors.toList());
    }

    private <T> void addBounded(Deque<T> deque, T entry) {
        deque.addFirst(entry);
        while (deque.size() > bufferSize) {
            deque.removeLast();
        }
    }
}
