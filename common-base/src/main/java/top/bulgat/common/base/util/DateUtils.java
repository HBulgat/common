package top.bulgat.common.base.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 基于 {@code java.time} API 的日期时间工具类。
 */
public final class DateUtils {

    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DATETIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String PATTERN_TIME = "HH:mm:ss";

    public static final DateTimeFormatter FORMATTER_DATE = DateTimeFormatter.ofPattern(PATTERN_DATE);
    public static final DateTimeFormatter FORMATTER_DATETIME = DateTimeFormatter.ofPattern(PATTERN_DATETIME);
    public static final DateTimeFormatter FORMATTER_DATETIME_MS = DateTimeFormatter.ofPattern(PATTERN_DATETIME_MS);
    public static final DateTimeFormatter FORMATTER_TIME = DateTimeFormatter.ofPattern(PATTERN_TIME);

    private DateUtils() {
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : FORMATTER_DATETIME.format(dateTime);
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime == null ? null : DateTimeFormatter.ofPattern(pattern).format(dateTime);
    }

    public static String format(LocalDate date) {
        return date == null ? null : FORMATTER_DATE.format(date);
    }

    public static LocalDateTime parseDateTime(String text) {
        return (text == null || text.isBlank()) ? null : LocalDateTime.parse(text, FORMATTER_DATETIME);
    }

    public static LocalDateTime parseDateTime(String text, String pattern) {
        return (text == null || text.isBlank()) ? null : LocalDateTime.parse(text, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseDate(String text) {
        return (text == null || text.isBlank()) ? null : LocalDate.parse(text, FORMATTER_DATE);
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date toDate(LocalDateTime dateTime) {
        return dateTime == null ? null : Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime ofEpochMilli(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static long toEpochMilli(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static String now() {
        return format(LocalDateTime.now());
    }

    public static String today() {
        return format(LocalDate.now());
    }

    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }
}
