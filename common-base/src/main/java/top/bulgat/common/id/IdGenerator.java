package top.bulgat.common.id;

/**
 * ID generator interface.
 * <p>
 * Provides a unified abstraction for various ID generation strategies.
 */
public interface IdGenerator {

    /**
     * Generate next unique ID as long.
     */
    long nextId();

    /**
     * Generate next unique ID as String.
     */
    default String nextIdStr() {
        return String.valueOf(nextId());
    }
}
