package top.bulgat.common.exception;

import org.junit.jupiter.api.Test;
import top.bulgat.common.base.exception.BizException;
import top.bulgat.common.base.exception.ErrorCode;

import static org.junit.jupiter.api.Assertions.*;

class BizExceptionTest {

    @Test
    void testWithMessage() {
        BizException ex = new BizException("test error");
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), ex.getCode());
        assertEquals("test error", ex.getMessage());
    }

    @Test
    void testWithCodeAndMessage() {
        BizException ex = new BizException(400, "bad param");
        assertEquals(400, ex.getCode());
        assertEquals("bad param", ex.getMessage());
    }

    @Test
    void testWithErrorCode() {
        BizException ex = new BizException(ErrorCode.NOT_FOUND);
        assertEquals(404, ex.getCode());
        assertEquals("not found", ex.getMessage());
    }

    @Test
    void testWithErrorCodeAndCustomMessage() {
        BizException ex = new BizException(ErrorCode.PARAM_ERROR, "name must not be empty");
        assertEquals(10400, ex.getCode());
        assertEquals("name must not be empty", ex.getMessage());
    }

    @Test
    void testWithErrorCodeAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        BizException ex = new BizException(ErrorCode.SYSTEM_ERROR, cause);
        assertEquals(500, ex.getCode());
        assertSame(cause, ex.getCause());
    }

    @Test
    void testWithCodeMessageAndCause() {
        RuntimeException cause = new RuntimeException("root");
        BizException ex = new BizException(503, "service down", cause);
        assertEquals(503, ex.getCode());
        assertEquals("service down", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
