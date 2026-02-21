package top.bulgat.common.notice.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Feishu custom card message (raw card JSON).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeishuCardMessage extends FeishuMessage {

    /**
     * Full card JSON structure (header + elements).
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
