package top.bulgat.common.notice.feishu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Feishu text message with @mention helpers.
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
     * Generate @mention tag for a specific user.
     *
     * @param userId Feishu user_id (e.g. "ou_xxx")
     * @param name   display name
     * @return at tag string
     */
    public static String atUser(String userId, String name) {
        return "<at user_id=\"" + userId + "\">" + name + "</at>";
    }

    /**
     * Generate @all tag to mention everyone in the group.
     */
    public static String atAll() {
        return "<at user_id=\"all\">所有人</at>";
    }
}
