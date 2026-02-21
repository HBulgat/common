package top.bulgat.common.id.impl;

import top.bulgat.common.id.IdGenerator;

import java.util.UUID;

/**
 * UUID-based ID generator.
 * <p>
 * Generates standard UUID strings (lowercase, no dashes).
 * The {@link #nextId()} method is not supported since UUIDs are string-based;
 * use {@link #nextIdStr()} instead.
 */
public class UuidIdGenerator implements IdGenerator {

    @Override
    public long nextId() {
        throw new UnsupportedOperationException("UUID generator does not support long ID. Use nextIdStr() instead.");
    }

    @Override
    public String nextIdStr() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
