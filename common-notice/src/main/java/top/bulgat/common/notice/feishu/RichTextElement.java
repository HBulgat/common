package top.bulgat.common.notice.feishu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.AccessLevel;

import java.io.Serializable;

/**
 * Rich text element for Feishu post messages.
 * <p>
 * This class serves as both the base class for different element types and a factory for creating them.
 * Constructors are private to enforce usage of static factory methods.
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RichTextElement implements Serializable {
    protected String tag;

    // --- Factory Methods ---

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

    // --- Inner Classes ---

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
