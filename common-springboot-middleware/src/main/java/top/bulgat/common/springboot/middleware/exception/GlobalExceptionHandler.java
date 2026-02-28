package top.bulgat.common.springboot.middleware.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.bulgat.common.base.exception.BizException;
import top.bulgat.common.base.model.Result;

import java.util.stream.Collectors;

/**
 * 全局异常处理器，将异常转换为统一的 {@link Result} 响应。
 * <ul>
 *   <li>{@link BizException} → 业务错误码 + 消息 (HTTP 200)</li>
 *   <li>参数校验错误 (Validation errors) → 返回 400，并聚合字段错误消息</li>
 *   <li>其他错误 (Everything else) → 返回 500，对客户端隐藏具体信息，在日志中记录完整堆栈</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务 / 领域异常 – 受控失败，不需要记录堆栈跟踪。
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("BizException: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理 @RequestBody 上的 @Valid / @Validated 异常。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", msg);
        return Result.fail(400, msg);
    }

    /**
     * 处理 @ModelAttribute / 表单绑定 上的 @Valid 异常。
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBind(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Bind failed: {}", msg);
        return Result.fail(400, msg);
    }

    /**
     * 这个方法捕获所有未处理的异常 – 对客户端隐藏内部细节，记录完整的堆栈跟踪。
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleAll(Throwable e) {
        log.error("Unhandled exception", e);
        return Result.fail(500, "系统繁忙，请稍后重试");
    }
}
