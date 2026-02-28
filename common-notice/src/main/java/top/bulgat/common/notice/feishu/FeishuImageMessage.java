package top.bulgat.common.notice.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 飞书图片消息。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeishuImageMessage extends FeishuMessage {

    /**
     * 从飞书上传接口获取的图片 key。
     */
    private String imageKey;

    @Override
    public String msgType() {
        return "image";
    }

    @Override
    protected Map<String, Object> specificPayload() {
        return Map.of(KEY_CONTENT, Map.of("image_key", imageKey));
    }
}
