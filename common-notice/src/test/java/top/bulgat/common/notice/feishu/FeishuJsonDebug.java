package top.bulgat.common.notice.feishu;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Debug: print the JSON payloads to verify structure.
 */
public class FeishuJsonDebug {


    @Test
    public void testSendMessage() throws Exception {
        ObjectMapper om = new ObjectMapper();

        // Post message
        RichTextContent richContent = RichTextContent.builder("Integration Test")
                .addLine(
                        RichTextElement.text("Service "),
                        RichTextElement.text("order-service"),
                        RichTextElement.text(" is running.")
                )
                .addLine(
                        RichTextElement.text("Dashboard: "),
                        RichTextElement.link("Grafana", "https://grafana.example.com")
                )
                .addLine(
                        RichTextElement.atAll(),
                        RichTextElement.text(" FYI")
                )
                .build();

        FeishuPostMessage postMsg = FeishuPostMessage.builder()
                .webhookUrl("http://example.com")
                .content(richContent)
                .build();
        
        Map<String, Object> payload = postMsg.toPayload();

        System.out.println("=== Post message JSON ===");
        System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(payload));
    }
}
