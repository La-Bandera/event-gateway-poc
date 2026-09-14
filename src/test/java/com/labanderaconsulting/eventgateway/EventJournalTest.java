package com.labanderaconsulting.eventgateway;

import com.labanderaconsulting.eventgateway.model.EdgeEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain unit test — no Quarkus context needed, this is just a bounded
 * in-memory buffer. Exercises the two properties the dashboard depends on:
 * most-recent-first ordering and the size cap.
 */
class EventJournalTest {

    private EventJournal journalWithCapacity(int size) {
        EventJournal journal = new EventJournal();
        try {
            Field field = EventJournal.class.getDeclaredField("bufferSize");
            field.setAccessible(true);
            field.set(journal, size);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return journal;
    }

    private EdgeEvent event(String id) {
        return new EdgeEvent(id, "facility-42", "package.scan", Map.of());
    }

    @Test
    void returnsMostRecentFirst() {
        EventJournal journal = journalWithCapacity(10);

        journal.recordRaw(event("evt-1"));
        journal.recordRaw(event("evt-2"));
        journal.recordRaw(event("evt-3"));

        List<EventJournal.Entry<EdgeEvent>> recent = journal.recentRaw();

        assertThat(recent).extracting(e -> e.event().id())
                .containsExactly("evt-3", "evt-2", "evt-1");
    }

    @Test
    void evictsTheOldestEntryOnceOverCapacity() {
        EventJournal journal = journalWithCapacity(2);

        journal.recordRaw(event("evt-1"));
        journal.recordRaw(event("evt-2"));
        journal.recordRaw(event("evt-3"));

        List<EventJournal.Entry<EdgeEvent>> recent = journal.recentRaw();

        assertThat(recent).extracting(e -> e.event().id())
                .containsExactly("evt-3", "evt-2");
    }

    @Test
    void rawAndProcessedBuffersAreIndependent() {
        EventJournal journal = journalWithCapacity(10);

        journal.recordRaw(event("evt-1"));

        assertThat(journal.recentRaw()).hasSize(1);
        assertThat(journal.recentProcessed()).isEmpty();
    }
}
