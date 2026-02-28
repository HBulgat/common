package top.bulgat.common.util;

import org.junit.jupiter.api.Test;
import top.bulgat.common.base.exception.ErrorCode;
import top.bulgat.common.base.exception.BizException;
import top.bulgat.common.base.util.ThrowUtils;

import static org.junit.jupiter.api.Assertions.*;

class ThrowUtilsTest {

    @Test
    void testThrowIf_true() {
        BizException ex = assertThrows(BizException.class,
                () -> ThrowUtils.throwIf(true, ErrorCode.PARAM_ERROR));
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void testThrowIf_false() {
        assertDoesNotThrow(() -> ThrowUtils.throwIf(false, ErrorCode.PARAM_ERROR));
    }

    @Test
    void testThrowIfWithMessage() {
        BizException ex = assertThrows(BizException.class,
                () -> ThrowUtils.throwIf(true, ErrorCode.PARAM_ERROR, "invalid name"));
        assertEquals("invalid name", ex.getMessage());
    }

    @Test
    void testThrowIfWithCodeAndMessage() {
        BizException ex = assertThrows(BizException.class,
                () -> ThrowUtils.throwIf(true, 10001, "custom error"));
        assertEquals(10001, ex.getCode());
    }

    @Test
    void testThrowIfWithStringMessage() {
        BizException ex = assertThrows(BizException.class,
                () -> ThrowUtils.throwIf(true, "something went wrong"));
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), ex.getCode());
    }

    @Test
    void testThrowIfNull() {
        assertThrows(BizException.class,
                () -> ThrowUtils.throwIfNull(null, ErrorCode.NOT_FOUND));
        assertDoesNotThrow(
                () -> ThrowUtils.throwIfNull("value", ErrorCode.NOT_FOUND));
    }

    @Test
    void testThrowIfBlank() {
        assertThrows(BizException.class,
                () -> ThrowUtils.throwIfBlank("", ErrorCode.PARAM_ERROR));
        assertThrows(BizException.class,
                () -> ThrowUtils.throwIfBlank("  ", ErrorCode.PARAM_ERROR));
        assertDoesNotThrow(
                () -> ThrowUtils.throwIfBlank("value", ErrorCode.PARAM_ERROR));
    }
}
