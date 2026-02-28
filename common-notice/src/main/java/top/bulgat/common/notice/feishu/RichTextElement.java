package top.bulgat.common.notice.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.AccessLevel;

import java.io.Serializable;

/**
 * 飞书富文本(post)消息的元素。
 * <p>
 * 此类既作为不同元素类型的基类，也作为创建它们的工厂类。
 * 构造函数是私有的，以强制使用静态工厂方法。
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RichTextElement implements Serializable {
    protected String tag;

    // --- 工厂方法 ---

    public static TextElement text(String text) {
        return new TextElement(text);
    }

    public static LinkElement link(String text, String href) {
        return new LinkElement(text, href);
    }

    public static AtElement at(String userId, String userName) {
        return new AtElement(userId, userName);
    }

    public static AtElement atAll() {
        return new AtElement("all", "所有人");
    }

    public static ImageElement image(String imageKey) {
        return new ImageElement(imageKey);
    }

    // --- 内部类 ---

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TextElement extends RichTextElement {
        private String text;
        
        private TextElement(String text) {
            super("text");
            this.text = text;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LinkElement extends RichTextElement {
        private String text;
        private String href;

        private LinkElement(String text, String href) {
            super("a");
            this.text = text;
            this.href = href;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AtElement extends RichTextElement {
        @JsonProperty("user_id")
        private String userId;
        @JsonProperty("user_name")
        private String userName;

        private AtElement(String userId, String userName) {
            super("at");
            this.userId = userId;
            this.userName = userName;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ImageElement extends RichTextElement {
        @JsonProperty("image_key")
        private String imageKey;

        private ImageElement(String imageKey) {
            super("img");
            this.imageKey = imageKey;
        }
    }
}
