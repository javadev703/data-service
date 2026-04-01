package com.here.data.dataService;

import com.here.data.dataService.aggregator.EventAggregator;
import com.here.data.dataService.dto.Event;
import com.here.data.dataService.dto.EventStats;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class EventAggregatorTest {

    @Test
    void testEmptyStream() {
        Map<String, EventStats> result = EventAggregator.aggregate(Stream.empty());
        assertTrue(result.isEmpty());
    }

    @Test
    void testSingleEvent() {
        Event e = new Event("A", 1000L, 10.0);

        Map<String, EventStats> result = EventAggregator.aggregate(Stream.of(e));

        EventStats stats = result.get("A");
        assertEquals(1, stats.count());
        assertEquals(1000L, stats.minTimestamp());
        assertEquals(1000L, stats.maxTimestamp());
        assertEquals(10.0, stats.average());
    }

    @Test
    void testDuplicates() {
        Event e1 = new Event("A", 1000L, 10.0);
        Event e2 = new Event("A", 1000L, 20.0); // duplicate timestamp

        Map<String, EventStats> result = EventAggregator.aggregate(Stream.of(e1, e2));

        EventStats stats = result.get("A");
        assertEquals(1, stats.count());
        assertEquals(10.0, stats.average());
    }

    @Test
    void testInvalidEvents() {
        Event valid = new Event("A", 1000L, 10.0);
        Event invalid = new Event("A", 2000L, -5.0);

        Map<String, EventStats> result = EventAggregator.aggregate(Stream.of(valid, invalid));

        EventStats stats = result.get("A");
        assertEquals(1, stats.count());
    }

    @Test
    void testMultipleIds() {
        Stream<Event> stream = Stream.of(
                new Event("A", 1000, 10),
                new Event("A", 2000, 20),
                new Event("B", 1500, 30)
        );

        Map<String, EventStats> result = EventAggregator.aggregate(stream);

        assertEquals(2, result.get("A").count());
        assertEquals(1, result.get("B").count());
    }

    @Test
    void testParallelExecution() {
        Stream<Event> stream = Stream.of(
                new Event("A", 1000, 10),
                new Event("A", 2000, 20),
                new Event("A", 3000, 30)
        ).parallel();

        Map<String, EventStats> result = EventAggregator.aggregate(stream);

        assertEquals(3, result.get("A").count());
        assertEquals(20.0, result.get("A").average());
    }
}