package top.bulgat.common.util;

import top.bulgat.common.exception.ErrorCode;
import top.bulgat.common.exception.BizException;

/**
 * 抛出异常的工具类，用于便捷地抛出异常。
 * <p>
 * 提供基于条件抛出 {@link BizException} 的简洁方法。
 */
public final class ThrowUtils {

    private ThrowUtils() {
    }

    /**
     * 如果条件为 true，则抛出 BizException。
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BizException(errorCode);
        }
    }

    /**
     * 如果条件为 true，则抛出带有自定义消息的 BizException。
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        if (condition) {
            throw new BizException(errorCode, message);
        }
    }

    /**
     * 如果条件为 true，则抛出带有错误码和消息的 BizException。
     */
    public static void throwIf(boolean condition, int code, String message) {
        if (condition) {
            throw new BizException(code, message);
        }
    }

    /**
     * 如果条件为 true，则抛出 SYSTEM_ERROR 的 BizException。
     */
    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, message);
        }
    }

    /**
     * 如果对象为空，则抛出 BizException。
     */
    public static void throwIfNull(Object obj, ErrorCode errorCode) {
        throwIf(obj == null, errorCode);
    }

    /**
     * 如果对象为空，则抛出带有自定义消息的 BizException。
     */
    public static void throwIfNull(Object obj, ErrorCode errorCode, String message) {
        throwIf(obj == null, errorCode, message);
    }

    /**
     * 如果字符串为空白（null、空或仅包含空格），则抛出 BizException。
     */
    public static void throwIfBlank(String str, ErrorCode errorCode) {
        throwIf(str == null || str.isBlank(), errorCode);
    }

    /**
     * 如果字符串为空白，则抛出带有自定义消息的 BizException。
     */
    public static void throwIfBlank(String str, ErrorCode errorCode, String message) {
        throwIf(str == null || str.isBlank(), errorCode, message);
    }
}
