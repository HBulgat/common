package top.bulgat.common.notice.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 飞书分享群名片消息。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeishuShareChatMessage extends FeishuMessage {

    /**
     * 要分享的群组 ID（例如 "oc_xxx"）。
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
