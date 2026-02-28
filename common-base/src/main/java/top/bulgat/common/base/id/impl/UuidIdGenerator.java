package top.bulgat.common.base.id.impl;

import top.bulgat.common.base.id.IdGenerator;

import java.util.UUID;

/**
 * 基于 UUID 的 ID 生成器。
 * <p>
 * 生成标准的 UUID 字符串 (小写，无连字符)。
 * 不支持 {@link #nextId()} 方法，因为 UUID 是基于字符串的；
 * 请使用 {@link #nextIdStr()} 代替。
 */
public class UuidIdGenerator implements IdGenerator {

    @Override
    public long nextId() {
        throw new UnsupportedOperationException("UUID generator does not support long ID. Use nextIdStr() instead.");
    }

    @Override
    public String nextIdStr() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
