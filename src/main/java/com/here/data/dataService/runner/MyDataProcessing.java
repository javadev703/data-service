package com.here.data.dataService.runner;

import com.here.data.dataService.aggregator.EventAggregator;
import com.here.data.dataService.dto.Event;
import com.here.data.dataService.dto.EventStats;

import java.util.Map;
import java.util.stream.Stream;

public class MyDataProcessing {
    public static void main(String[] args) {

        Stream<Event> events = Stream.of(
                new Event("A", 1000L, 10.0),
                new Event("A", 2000L, 20.0),
                new Event("A", 1000L, 30.0), // duplicate (ignored)
                new Event("B", 1500L, 5.0),
                new Event("B", 2500L, -1.0), // invalid (ignored)
                new Event("C", 3000L, Double.NaN) // invalid (ignored)
        );

        Map<String, EventStats> result = EventAggregator.aggregate(
                events.parallel() // supports parallel execution
        );

        result.forEach((id, stats) -> {
            System.out.println("ID: " + id);
            System.out.println("  Count: " + stats.count());
            System.out.println("  Min Timestamp: " + stats.minTimestamp());
            System.out.println("  Max Timestamp: " + stats.maxTimestamp());
            System.out.println("  Average: " + stats.average());
        });
    }
}
