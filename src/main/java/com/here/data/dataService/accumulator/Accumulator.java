package com.here.data.dataService.accumulator;

import com.here.data.dataService.dto.Event;
import com.here.data.dataService.dto.EventStats;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Accumulator {
    private long count = 0;
    private long minTimestamp = Long.MAX_VALUE;
    private long maxTimestamp = Long.MIN_VALUE;
    private double sum = 0.0;

    // Deduplication: timestamps per ID
    private final Set<Long> seenTimestamps = ConcurrentHashMap.newKeySet();

    public synchronized void add(Event e) {
        if (!seenTimestamps.add(e.timestamp())) {
            return; // duplicate
        }

        count++;
        sum += e.value();
        minTimestamp = Math.min(minTimestamp, e.timestamp());
        maxTimestamp = Math.max(maxTimestamp, e.timestamp());
    }

    public synchronized void merge(Accumulator other) {
        for (Long ts : other.seenTimestamps) {
            if (this.seenTimestamps.add(ts)) {
                this.count++;
            }
        }
        this.sum += other.sum;
        this.minTimestamp = Math.min(this.minTimestamp, other.minTimestamp);
        this.maxTimestamp = Math.max(this.maxTimestamp, other.maxTimestamp);
    }

    public EventStats toStats() {
        return new EventStats(
                count,
                count == 0 ? 0 : minTimestamp,
                count == 0 ? 0 : maxTimestamp,
                count == 0 ? 0.0 : sum / count
        );
    }
}
