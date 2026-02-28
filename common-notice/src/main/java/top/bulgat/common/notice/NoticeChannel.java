package top.bulgat.common.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支持的通知渠道。
 */
@Getter
@AllArgsConstructor
public enum NoticeChannel {

    FEISHU("feishu"),
    EMAIL("email");

    private final String value;
}
