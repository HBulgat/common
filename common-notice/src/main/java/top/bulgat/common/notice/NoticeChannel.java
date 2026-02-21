package top.bulgat.common.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Supported notice channels.
 */
@Getter
@AllArgsConstructor
public enum NoticeChannel {

    FEISHU("feishu"),
    EMAIL("email");

    private final String value;
}
