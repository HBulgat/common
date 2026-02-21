package top.bulgat.common.util;

import top.bulgat.common.exception.ErrorCode;
import top.bulgat.common.exception.BizException;

/**
 * Throw utility for convenient exception throwing.
 * <p>
 * Provides concise methods to throw {@link BizException} based on conditions.
 */
public final class ThrowUtils {

    private ThrowUtils() {
    }

    /**
     * Throw BizException if condition is true.
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BizException(errorCode);
        }
    }

    /**
     * Throw BizException with custom message if condition is true.
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        if (condition) {
            throw new BizException(errorCode, message);
        }
    }

    /**
     * Throw BizException with code and message if condition is true.
     */
    public static void throwIf(boolean condition, int code, String message) {
        if (condition) {
            throw new BizException(code, message);
        }
    }

    /**
     * Throw BizException with SYSTEM_ERROR if condition is true.
     */
    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, message);
        }
    }

    /**
     * Throw BizException if object is null.
     */
    public static void throwIfNull(Object obj, ErrorCode errorCode) {
        throwIf(obj == null, errorCode);
    }

    /**
     * Throw BizException if object is null, with custom message.
     */
    public static void throwIfNull(Object obj, ErrorCode errorCode, String message) {
        throwIf(obj == null, errorCode, message);
    }

    /**
     * Throw BizException if string is blank (null, empty, or whitespace only).
     */
    public static void throwIfBlank(String str, ErrorCode errorCode) {
        throwIf(str == null || str.isBlank(), errorCode);
    }

    /**
     * Throw BizException if string is blank, with custom message.
     */
    public static void throwIfBlank(String str, ErrorCode errorCode, String message) {
        throwIf(str == null || str.isBlank(), errorCode, message);
    }
}
