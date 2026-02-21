package top.bulgat.common.id;

import org.junit.jupiter.api.Test;
import top.bulgat.common.id.impl.UuidIdGenerator;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UuidIdGeneratorTest {

    @Test
    void testNextIdStr() {
        IdGenerator generator = new UuidIdGenerator();
        String id = generator.nextIdStr();
        assertNotNull(id);
        assertEquals(32, id.length());
        assertFalse(id.contains("-"));
    }

    @Test
    void testUniqueness() {
        UuidIdGenerator generator = new UuidIdGenerator();
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            ids.add(generator.nextIdStr());
        }
        assertEquals(10000, ids.size());
    }

    @Test
    void testNextIdThrows() {
        UuidIdGenerator generator = new UuidIdGenerator();
        assertThrows(UnsupportedOperationException.class, generator::nextId);
    }
}
