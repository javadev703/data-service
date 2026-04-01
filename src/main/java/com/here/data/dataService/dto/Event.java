package com.here.data.dataService.dto;

public record Event(
        String id,
        long timestamp,
        double value
) {}