package top.bulgat.common.notice.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Feishu rich text (post) message.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeishuPostMessage extends FeishuMessage {

    /**
     * Structured rich text content.
     */
    private RichTextContent content;

    @Override
    public String msgType() {
        return "post";
    }

    @Override
    protected Map<String, Object> specificPayload() {
        // We need to return the content map directly.
        // The structure is content: { post: { zh_cn: { title:..., content:... } } }
        // RichTextContent.toObject returns POJO with title/content.
        
        RichTextContent.RichTextContentObject contentObj = content.toObject();
        return Map.of(KEY_CONTENT, Map.of("post", Map.of("zh_cn", contentObj)));
    }
}
