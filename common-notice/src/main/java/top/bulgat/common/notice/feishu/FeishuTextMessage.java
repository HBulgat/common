package top.bulgat.common.notice.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 带有 @ 提醒辅助功能的飞书纯文本消息。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FeishuTextMessage extends FeishuMessage {

    private String text;

    @Override
    public String msgType() {
        return "text";
    }

    @Override
    protected Map<String, Object> specificPayload() {
        return Map.of(KEY_CONTENT, Map.of("text", text));
    }

    /**
     * 为特定用户生成 @ 提醒标签。
     *
     * @param userId 飞书 user_id（例如 "ou_xxx"）
     * @param name   显示名称
     * @return at 标签字符串
     */
    public static String atUser(String userId, String name) {
        return "<at user_id=\"" + userId + "\">" + name + "</at>";
    }

    /**
     * 生成 @all 标签以提醒群组中的所有人。
     */
    public static String atAll() {
        return "<at user_id=\"all\">所有人</at>";
    }
}
