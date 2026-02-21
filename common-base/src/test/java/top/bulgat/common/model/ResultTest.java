package top.bulgat.common.model;

import org.junit.jupiter.api.Test;
import top.bulgat.common.exception.ErrorCode;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void testSuccess() {
        Result<Void> result = Result.success();
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    void testSuccessWithData() {
        Result<String> result = Result.success("hello");
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals("hello", result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    void testSuccessWithMessageAndData() {
        Result<Integer> result = Result.success("done", 42);
        assertEquals(ErrorCode.SUCCESS.getCode(), result.getCode());
        assertEquals("done", result.getMessage());
        assertEquals(42, result.getData());
    }

    @Test
    void testFailWithErrorCode() {
        Result<Void> result = Result.fail(ErrorCode.NOT_FOUND);
        assertEquals(404, result.getCode());
        assertEquals("not found", result.getMessage());
        assertFalse(result.isSuccess());
    }

    @Test
    void testFailWithCodeAndMessage() {
        Result<Void> result = Result.fail(400, "bad param");
        assertEquals(400, result.getCode());
        assertFalse(result.isSuccess());
    }

    @Test
    void testFailDefault() {
        Result<Void> result = Result.fail("error occurred");
        assertEquals(ErrorCode.SYSTEM_ERROR.getCode(), result.getCode());
        assertFalse(result.isSuccess());
    }
}
