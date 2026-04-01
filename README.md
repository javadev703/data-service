# data-service

# Event Aggregation Library

## Overview

This library processes a stream of events and computes aggregated statistics per `id`.
It is designed to work efficiently with large datasets by using Java streams instead of loading everything into memory.

For each `id`, it calculates:

* Count of valid events
* Minimum and maximum timestamp
* Average value

It also handles:

* Invalid data filtering
* Deduplication (based on `id + timestamp`)
* Parallel execution

---

I used standard Java 17 with no external dependencies.

## Running the Application

A runnable class is provided  - runner.MyDataProcessing

javac MyDataProcessing.java

java MyDataProcessing

## Test
Running the tests will execute all unit test cases covering edge scenarios like empty input, duplicates, and parallel execution.

---

## Design Decisions

### 1. Stream-based processing

The method takes a `Stream<Event>` instead of a `List`.

This was intentional because:

* it avoids loading all data into memory
* it works well with large datasets
* it allows both sequential and parallel execution

---

### 2. Custom Collector

Instead of using `groupingBy`, I implemented a custom `Collector`.

Reason:

* I needed more control over aggregation logic
* built-in collectors don’t support deduplication easily
* it helps in handling parallel merging explicitly

---

### 3. Thread Safety

* A `ConcurrentHashMap` is used to group events by `id`
* Each `id` has its own accumulator
* Updates inside the accumulator are synchronized

This ensures correctness when using parallel streams.



### 4. Deduplication

Duplicates are identified using `(id + timestamp)`.

Each accumulator keeps a set of seen timestamps and ignores duplicates.


### 5. Memory Consideration

The implementation does not store the entire dataset, but it does store:

* one accumulator per `id`
* a set of timestamps per `id` (for deduplication)

So memory grows with:

* number of unique IDs
* number of unique timestamps

---

## Assumptions

* Events with:

    * null `id`
    * `NaN` value
    * negative value
      are considered invalid and ignored

* Duplicate means same `id` and same `timestamp`

* If duplicates exist, only the first occurrence is processed

* Event order does not matter
