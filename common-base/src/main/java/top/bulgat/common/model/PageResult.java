package top.bulgat.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Paginated response wrapper.
 *
 * @param <T> list element type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int pageNum;

    private int pageSize;

    private long total;

    private List<T> list;

    public static <T> PageResult<T> of(int pageNum, int pageSize, long total, List<T> list) {
        return new PageResult<>(pageNum, pageSize, total, list);
    }

    public static <T> PageResult<T> empty(int pageNum, int pageSize) {
        return new PageResult<>(pageNum, pageSize, 0, Collections.emptyList());
    }

    public int getTotalPages() {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }

    public boolean hasNext() {
        return pageNum < getTotalPages();
    }
}
