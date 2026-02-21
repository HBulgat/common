package top.bulgat.common.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void testFormatLocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
        assertEquals("2024-06-15 14:30:00", DateUtils.format(dt));
        assertNull(DateUtils.format((LocalDateTime) null));
    }

    @Test
    void testFormatWithPattern() {
        LocalDateTime dt = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
        assertEquals("2024/06/15", DateUtils.format(dt, "yyyy/MM/dd"));
    }

    @Test
    void testFormatLocalDate() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        assertEquals("2024-06-15", DateUtils.format(date));
        assertNull(DateUtils.format((LocalDate) null));
    }

    @Test
    void testParseDateTime() {
        LocalDateTime dt = DateUtils.parseDateTime("2024-06-15 14:30:00");
        assertNotNull(dt);
        assertEquals(2024, dt.getYear());
        assertEquals(6, dt.getMonthValue());
        assertEquals(15, dt.getDayOfMonth());
        assertEquals(14, dt.getHour());
        assertNull(DateUtils.parseDateTime(null));
        assertNull(DateUtils.parseDateTime(""));
    }

    @Test
    void testParseDate() {
        LocalDate date = DateUtils.parseDate("2024-06-15");
        assertNotNull(date);
        assertEquals(2024, date.getYear());
        assertNull(DateUtils.parseDate(null));
    }

    @Test
    void testDateConversion() {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Date date = DateUtils.toDate(now);
        assertNotNull(date);
        LocalDateTime converted = DateUtils.toLocalDateTime(date);
        assertEquals(now, converted);
    }

    @Test
    void testEpochMilli() {
        LocalDateTime dt = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        long millis = DateUtils.toEpochMilli(dt);
        LocalDateTime back = DateUtils.ofEpochMilli(millis);
        assertEquals(dt, back);
    }

    @Test
    void testNowAndToday() {
        String now = DateUtils.now();
        assertNotNull(now);
        assertTrue(now.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));

        String today = DateUtils.today();
        assertNotNull(today);
        assertTrue(today.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void testStartAndEndOfDay() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        LocalDateTime start = DateUtils.startOfDay(date);
        assertEquals(0, start.getHour());
        assertEquals(0, start.getMinute());

        LocalDateTime end = DateUtils.endOfDay(date);
        assertEquals(23, end.getHour());
        assertEquals(59, end.getMinute());
        assertEquals(59, end.getSecond());
    }
}
