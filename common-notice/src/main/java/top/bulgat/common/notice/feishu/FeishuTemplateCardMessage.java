package top.bulgat.common.notice.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 飞书搭建工具生成的卡片模板消息。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeishuTemplateCardMessage extends FeishuMessage {

    /**
     * 飞书卡片搭建工具中的模板 ID。
     */
    private String templateId;

    /**
     * 模板版本名（可选）。
     */
    private String templateVersionName;

    /**
     * 需要填充的模板变量。
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
