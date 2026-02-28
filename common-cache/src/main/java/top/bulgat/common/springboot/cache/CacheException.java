package top.bulgat.common.springboot.cache;

/**
 * 缓存操作失败时的非受检异常。
 */
public class CacheException extends RuntimeException {

    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
