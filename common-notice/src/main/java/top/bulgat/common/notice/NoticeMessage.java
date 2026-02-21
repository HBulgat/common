package top.bulgat.common.notice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base notice message.
 * <p>
 * Subclasses define channel-specific fields.
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
     * Target channel.
     */
    private NoticeChannel channel;
}
