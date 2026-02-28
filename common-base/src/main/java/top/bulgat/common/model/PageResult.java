package top.bulgat.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应包装类。
 *
 * @param <T> 列表元素类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int current;

    private int size;

    private long total;

    private List<T> list;

    public static <T> PageResult<T> of(int current, int size, long total, List<T> list) {
        return new PageResult<>(current, size, total, list);
    }

    public static <T> PageResult<T> empty(int current, int size) {
        return new PageResult<>(current, size, 0, Collections.emptyList());
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public int getTotalPages() {
        if (size <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / size);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean hasNext() {
        return current < getTotalPages();
    }
}
