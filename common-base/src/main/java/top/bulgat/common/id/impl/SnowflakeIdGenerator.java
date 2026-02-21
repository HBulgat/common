package top.bulgat.common.id.impl;

import lombok.Getter;
import top.bulgat.common.id.IdGenerator;
import top.bulgat.common.time.SystemClock;

/**
 * Snowflake algorithm ID generator.
 * <p>
 * Generates 64-bit globally unique, time-ordered long IDs.
 * <pre>
 * Structure (64 bit):
 * 0 - 41 bit timestamp - 5 bit datacenter - 5 bit worker - 12 bit sequence
 * </pre>
 * Thread-safe.
 */
public class SnowflakeIdGenerator implements IdGenerator {

    private static final long DATACENTER_ID_BITS = 5L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /** Epoch: 2024-01-01 00:00:00 UTC */
    private static final long EPOCH = 1704067200000L;

    @Getter
    private final long datacenterId;
    @Getter
    private final long workerId;

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                    "workerId must be between 0 and " + MAX_WORKER_ID + ", got: " + workerId);
        }
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException(
                    "datacenterId must be between 0 and " + MAX_DATACENTER_ID + ", got: " + datacenterId);
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public SnowflakeIdGenerator() {
        this(0, 0);
    }

    @Override
    public synchronized long nextId() {
        long timestamp = SystemClock.millisClock().now();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    "Clock moved backwards. Refusing to generate ID for " + (lastTimestamp - timestamp) + " ms");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = SystemClock.millisClock().now();
        while (timestamp <= lastTimestamp) {
            timestamp = SystemClock.millisClock().now();
        }
        return timestamp;
    }
}
