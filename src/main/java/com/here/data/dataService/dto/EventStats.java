package com.here.data.dataService.dto;

public record EventStats(
        long count,
        long minTimestamp,
        long maxTimestamp,
        double average
) {}