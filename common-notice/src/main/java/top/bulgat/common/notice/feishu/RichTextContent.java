package top.bulgat.common.notice.feishu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Rich text content builder for Feishu post messages.
 */
public class RichTextContent {

    private final String title;
    private final String locale;
    private final List<List<RichTextElement>> lines;

    private RichTextContent(String title, String locale, List<List<RichTextElement>> lines) {
        this.title = title;
        this.locale = locale;
        this.lines = lines;
    }

    public static Builder builder(String title) {
        return new Builder(title, "zh_cn");
    }

    public static Builder builder(String title, String locale) {
        return new Builder(title, locale);
    }
    public RichTextContentObject toObject() {
        return new RichTextContentObject(title, lines);
    }
    
    // Helper POJO for serialization
    public static class RichTextContentObject {
        public String title;
        public List<List<RichTextElement>> content;
        
        public RichTextContentObject(String title, List<List<RichTextElement>> content) {
            this.title = title;
            this.content = content;
        }
    }

    public static class Builder {
        private final String title;
        private final String locale;
        private final List<List<RichTextElement>> lines = new ArrayList<>();

        private Builder(String title, String locale) {
            this.title = title;
            this.locale = locale;
        }

        public Builder addLine(RichTextElement... elements) {
            lines.add(Arrays.asList(elements));
            return this;
        }

        public RichTextContent build() {
            return new RichTextContent(title, locale, lines);
        }
    }
}
