package top.bulgat.common.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 公共错误码枚举。
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    /** 成功 */
    SUCCESS(200, "success"),
    /** 请求错误 */
    BAD_REQUEST(400, "bad request"),
    /** 未授权 */
    UNAUTHORIZED(401, "unauthorized"),
    /** 拒绝访问 */
    FORBIDDEN(403, "forbidden"),
    /** 未找到 */
    NOT_FOUND(404, "not found"),
    /** 方法不允许 */
    METHOD_NOT_ALLOWED(405, "method not allowed"),
    /** 冲突 */
    CONFLICT(409, "conflict"),
    /** 请求过多 */
    TOO_MANY_REQUESTS(429, "too many requests"),
    /** 参数校验失败 */
    PARAM_ERROR(10400, "parameter validation failed"),
    /** 系统内部错误 */
    SYSTEM_ERROR(500, "internal server error"),
    /** 服务不可用 */
    SERVICE_UNAVAILABLE(503, "service unavailable");

    private final int code;
    private final String message;
}
