package top.bulgat.common.notice.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import top.bulgat.common.notice.NoticeChannel;
import top.bulgat.common.notice.NoticeMessage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base class for all Feishu webhook messages.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class FeishuMessage extends NoticeMessage {

    protected static final String KEY_MSG_TYPE = "msg_type";
    protected static final String KEY_CONTENT = "content";
    protected static final String KEY_CARD = "card";

    /**
     * Webhook URL for the custom bot.
     */
    private String webhookUrl;

    /**
     * Feishu msg_type value.
     */
    public abstract String msgType();

    /**
     * Build the full payload map for this message.
     *
     * @return the complete payload map
     */
    public Map<String, Object> toPayload() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(KEY_MSG_TYPE, msgType());
        body.putAll(specificPayload());
        return body;
    }

    /**
     * Provide the message-specific part of the payload (e.g., "content" or "card").
     *
     * @return message-specific payload map
     */
    protected abstract Map<String, Object> specificPayload();

    @Override
    public NoticeChannel getChannel() {
        return NoticeChannel.FEISHU;
    }
}
