package top.bulgat.common.notice.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 飞书自定义卡片消息（原始卡片 JSON）。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeishuCardMessage extends FeishuMessage {

    /**
     * 完整的卡片 JSON 结构（header + elements）。
     */
    private Map<String, Object> card;

    @Override
    public String msgType() {
        return "interactive";
    }

    @Override
    protected Map<String, Object> specificPayload() {
        return Map.of(KEY_CARD, card);
    }
}
