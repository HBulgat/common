package top.bulgat.common.notice.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 飞书富文本 (post) 消息。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeishuPostMessage extends FeishuMessage {

    /**
     * 结构化的富文本内容。
     */
    private RichTextContent content;

    @Override
    public String msgType() {
        return "post";
    }

    @Override
    protected Map<String, Object> specificPayload() {
        // 我们需要直接返回内容 map。
        // 结构为 content: { post: { zh_cn: { title:..., content:... } } }
        // RichTextContent.toObject 返回带有 title/content 的 POJO。
        
        RichTextContent.RichTextContentObject contentObj = content.toObject();
        return Map.of(KEY_CONTENT, Map.of("post", Map.of("zh_cn", contentObj)));
    }
}
