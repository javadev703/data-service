package com.here.data.dataService.aggregator;

import com.here.data.dataService.accumulator.Accumulator;
import com.here.data.dataService.dto.Event;
import com.here.data.dataService.dto.EventStats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EventAggregator {

    public static Map<String, EventStats> aggregate(Stream<Event> events) {
        return events
                .filter(EventAggregator::isValid)
                .collect(Collector.of(
                        ConcurrentHashMap<String, Accumulator>::new,
                        EventAggregator::accumulate,
                        EventAggregator::combine,
                        EventAggregator::finish,
                        Collector.Characteristics.CONCURRENT,
                        Collector.Characteristics.UNORDERED
                ));
    }

    private static boolean isValid(Event e) {
        return e != null
                && e.id() != null
                && !Double.isNaN(e.value())
                && e.value() >= 0;
    }

    private static void accumulate(Map<String, Accumulator> map, Event e) {
        map.computeIfAbsent(e.id(), k -> new Accumulator()).add(e);
    }

    private static Map<String, Accumulator> combine(
            Map<String, Accumulator> m1,
            Map<String, Accumulator> m2
    ) {
        m2.forEach((id, acc2) ->
                m1.merge(id, acc2, (acc1, a2) -> {
                    acc1.merge(a2);
                    return acc1;
                })
        );
        return m1;
    }

    private static Map<String, EventStats> finish(Map<String, Accumulator> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toStats()
                ));
    }
}