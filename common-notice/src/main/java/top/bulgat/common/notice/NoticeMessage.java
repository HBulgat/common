package top.bulgat.common.notice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 基础通知消息。
 * <p>
 * 子类定义特定渠道的字段。
 *
 * @see top.bulgat.common.notice.feishu.FeishuTextMessage
 * @see top.bulgat.common.notice.feishu.FeishuPostMessage
 * @see top.bulgat.common.notice.feishu.FeishuImageMessage
 * @see top.bulgat.common.notice.feishu.FeishuCardMessage
 * @see top.bulgat.common.notice.email.EmailMessage
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class NoticeMessage {

    /**
     * 目标渠道。
     */
    private NoticeChannel channel;
}
