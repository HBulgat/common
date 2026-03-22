package top.bulgat.common.base.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 基于 Jackson 的 JSON 工具类。
 */
@Slf4j
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private JsonUtils() {
    }

    public static ObjectMapper getObjectMapper() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON serialization failed for object: {}", obj.getClass().getSimpleName(), e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * 对象转格式化 JSON 字符串 (用于日志打印或调试)
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON pretty serialization failed", e);
            throw new RuntimeException("JSON pretty serialization failed", e);
        }
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON deserialization failed for class: {}, json: {}", clazz.getSimpleName(), json, e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * JSON 字符串转复杂类型 (使用 TypeReference)
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON deserialization failed for typeReference, json: {}", json, e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * JSON 转 Map
     */
    public static Map<String, Object> toMap(String json) {
        return fromJson(json, Map.class);
    }

    /**
     * JSON 转 Map (泛型定制版 - 推荐)
     * 允许指定 Key 和 Value 的具体类型，调用方无需手动强转。
     *
     * @param json JSON 字符串
     * @param keyClass Key 的类型 (通常是 String.class)
     * @param valueClass Value 的类型 (如 String.class, Integer.class, User.class 等)
     * @return 指定泛型的 Map
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(
                    json,
                    OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyClass, valueClass)
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to Map<{}, {}>. Json: {}", keyClass.getSimpleName(), valueClass.getSimpleName(), json, e);
            throw new RuntimeException("Failed to parse JSON to Map", e);
        }
    }

    /**
     * JSON 转 Map (指定 Value 类型版 - 推荐)
     * Key 固定为 String，Value 指定具体类型。
     *
     * @param json JSON 字符串
     * @param valueClass Value 的目标类型 (如 String.class, Integer.class, User.class)
     * @return Map<String, V>
     */
    public static <V> Map<String, V> toMap(String json, Class<V> valueClass) {
        return toMap(json,String.class,valueClass);
    }

    /**
     * JSON 转 List
     * @param json JSON 字符串
     * @return 解析后的 List，若解析失败或输入为空则返回空列表
     */
    public static <T> List<T> toList(String json, Class<T> elementClass) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(
                    json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass)
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to List<{}>. Json: {}", elementClass.getSimpleName(), json, e);
            throw new RuntimeException("Failed to parse JSON to List", e);
        }
    }

    /**
     * 对象转换 (例如 Map 转 Bean, Bean 转另一个 Bean)
     */
    public static <T> T convert(Object obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        }
        try {
            return OBJECT_MAPPER.convertValue(obj, clazz);
        } catch (IllegalArgumentException e) {
            log.error("Object conversion failed from {} to {}", obj.getClass().getSimpleName(), clazz.getSimpleName(), e);
            throw new RuntimeException("Object conversion failed", e);
        }
    }

    /**
     * 【新增】安全解析：解析失败返回 null 而不是抛异常
     * 适用于容错场景，如缓存读取、非关键配置解析
     */
    public static <T> T fromJsonSafe(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.warn("Safe JSON deserialization failed, returning null. Json: {}", json, e);
            return null;
        }
    }
}
