package top.bulgat.common.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支持的通知渠道。
 */
@Getter
@AllArgsConstructor
public enum NoticeChannel {

    FEISHU,
    EMAIL;


    public static NoticeChannel fromValue(String value){
        return valueOf(value.toUpperCase());
    }
}
