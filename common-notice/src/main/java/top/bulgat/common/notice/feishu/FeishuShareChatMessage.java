package top.bulgat.common.notice.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Feishu share chat (group card) message.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeishuShareChatMessage extends FeishuMessage {

    /**
     * Chat ID to share (e.g. "oc_xxx").
     */
    private String shareChatId;

    @Override
    public String msgType() {
        return "share_chat";
    }

    @Override
    protected Map<String, Object> specificPayload() {
        return Map.of(KEY_CONTENT, Map.of("share_chat_id", shareChatId));
    }
}
