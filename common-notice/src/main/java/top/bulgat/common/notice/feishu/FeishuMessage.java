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
 * 所有飞书 Webhook 消息的基类。
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
     * 自定义机器人的 Webhook URL。
     */
    private String webhookUrl;

    /**
     * 飞书的 msg_type 值。
     */
    public abstract String msgType();

    /**
     * 构建此消息的完整载荷(Payload) Map。
     *
     * @return 完整的载荷(Payload) Map
     */
    public Map<String, Object> toPayload() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(KEY_MSG_TYPE, msgType());
        body.putAll(specificPayload());
        return body;
    }

    /**
     * 提供载荷中特定于消息的部分（例如 "content" 或 "card"）。
     *
     * @return 特定于消息的载荷 Map
     */
    protected abstract Map<String, Object> specificPayload();

    @Override
    public NoticeChannel getChannel() {
        return NoticeChannel.FEISHU;
    }
}
