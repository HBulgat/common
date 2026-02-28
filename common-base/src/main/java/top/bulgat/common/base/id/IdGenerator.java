package top.bulgat.common.base.id;

/**
 * ID 生成器接口。
 * <p>
 * 提供各种 ID 生成策略的统一抽象。
 */
public interface IdGenerator {

    /**
     * 生成下一个唯一的 long 类型 ID。
     */
    long nextId();

    /**
     * 生成下一个唯一的 String 类型 ID。
     */
    default String nextIdStr() {
        return String.valueOf(nextId());
    }
}
