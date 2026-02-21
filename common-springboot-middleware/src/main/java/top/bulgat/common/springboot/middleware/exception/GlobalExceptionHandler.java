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
import top.bulgat.common.exception.BizException;
import top.bulgat.common.model.Result;

import java.util.stream.Collectors;

/**
 * Global exception handler that converts exceptions to unified {@link Result} responses.
 * <ul>
 *   <li>{@link BizException} → business error code + message (HTTP 200)</li>
 *   <li>Validation errors → 400 with aggregated field messages</li>
 *   <li>Everything else → 500, message hidden from client, full stack in log</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Business / domain exceptions – controlled failures, no stack trace needed.
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("BizException: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * @Valid / @Validated on @RequestBody.
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
     * @Valid on @ModelAttribute / form binding.
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
     * Catch-all – hide internal details from client, log full trace.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleAll(Throwable e) {
        log.error("Unhandled exception", e);
        return Result.fail(500, "系统繁忙，请稍后重试");
    }
}
