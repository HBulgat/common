package top.bulgat.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Common error code enum.
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "bad request"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    NOT_FOUND(404, "not found"),
    METHOD_NOT_ALLOWED(405, "method not allowed"),
    CONFLICT(409, "conflict"),
    TOO_MANY_REQUESTS(429, "too many requests"),
    PARAM_ERROR(10400, "parameter validation failed"),
    SYSTEM_ERROR(500, "internal server error"),
    SERVICE_UNAVAILABLE(503, "service unavailable");

    private final int code;
    private final String message;
}
