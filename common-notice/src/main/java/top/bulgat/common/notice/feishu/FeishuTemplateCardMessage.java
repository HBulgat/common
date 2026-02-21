package top.bulgat.common.notice.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Feishu card template message.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeishuTemplateCardMessage extends FeishuMessage {

    /**
     * Card template ID from Feishu Card Builder.
     */
    private String templateId;

    /**
     * Template version name (optional).
     */
    private String templateVersionName;

    /**
     * Template variables to fill.
     */
    private Map<String, Object> templateVariable;

    @Override
    public String msgType() {
        return "interactive";
    }

    @Override
    protected Map<String, Object> specificPayload() {
        Map<String, Object> cardData = new LinkedHashMap<>();
        cardData.put("type", "template");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("template_id", templateId);
        if (templateVersionName != null) {
            data.put("template_version_name", templateVersionName);
        }
        if (templateVariable != null) {
            data.put("template_variable", templateVariable);
        }

        cardData.put("data", data);
        return Map.of(KEY_CARD, cardData);
    }
}
