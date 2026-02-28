package top.bulgat.common.base.exception;

import lombok.Getter;

/**
 * 业务异常。
 * <p>
 * 当业务逻辑验证失败时抛出该异常，例如参数无效，资源未找到等。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        super(message);
        this.code = ErrorCode.SYSTEM_ERROR.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
    }

    public BizException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
