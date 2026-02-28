package top.bulgat.common.util;

import org.junit.jupiter.api.Test;
import top.bulgat.common.base.util.StringUtils;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void testIsEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("hello"));
    }

    @Test
    void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   "));
        assertFalse(StringUtils.isBlank("hello"));
    }

    @Test
    void testTrim() {
        assertNull(StringUtils.trim(null));
        assertEquals("hello", StringUtils.trim("  hello  "));
    }

    @Test
    void testTrimToEmpty() {
        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("hello", StringUtils.trimToEmpty("  hello  "));
    }

    @Test
    void testCamelToUnderscore() {
        assertEquals("user_name", StringUtils.camelToUnderscore("userName"));
        assertEquals("create_time", StringUtils.camelToUnderscore("createTime"));
        assertEquals("id", StringUtils.camelToUnderscore("id"));
        assertNull(StringUtils.camelToUnderscore(null));
    }

    @Test
    void testUnderscoreToCamel() {
        assertEquals("userName", StringUtils.underscoreToCamel("user_name"));
        assertEquals("createTime", StringUtils.underscoreToCamel("create_time"));
        assertEquals("id", StringUtils.underscoreToCamel("id"));
        assertNull(StringUtils.underscoreToCamel(null));
    }

    @Test
    void testTruncate() {
        assertNull(StringUtils.truncate(null, 10));
        assertEquals("hello", StringUtils.truncate("hello", 10));
        assertEquals("hel...", StringUtils.truncate("hello world", 3));
    }

    @Test
    void testCapitalize() {
        assertEquals("Hello", StringUtils.capitalize("hello"));
        assertNull(StringUtils.capitalize(null));
    }

    @Test
    void testUncapitalize() {
        assertEquals("hello", StringUtils.uncapitalize("Hello"));
        assertNull(StringUtils.uncapitalize(null));
    }
}
