package top.bulgat.common.notice.feishu;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for Feishu notice sender.
 * <p>
 * These tests send real messages to a Feishu webhook and are disabled by default.
 */
@Disabled("Integration tests requiring real webhook")
public class FeishuIntegrationTest {

    private static final String WEBHOOK_URL =
            "https://open.feishu.cn/open-apis/bot/v2/hook/e3fe05da-fba0-4abe-8fea-2def5f2ae8fd";

    private final FeishuNoticeSender sender = new FeishuNoticeSender();

    @Test
    void testSendTextMessage() {
        FeishuTextMessage textMsg = FeishuTextMessage.builder()
                .webhookUrl(WEBHOOK_URL)
                .text("Hello from JUnit 5! " + FeishuTextMessage.atAll())
                .build();
        assertTrue(sender.send(textMsg));
    }

    @Test
    void testSendPostMessage() {
        RichTextContent richContent = RichTextContent.builder("JUnit 5 Integration Test")
                .addLine(
                        RichTextElement.text("Service "),
                        RichTextElement.text("common-notice"),
                        RichTextElement.text(" is testing post message.")
                )
                .addLine(
                        RichTextElement.text("Docs: "),
                        RichTextElement.link("Feishu Open Platform", "https://open.feishu.cn")
                )
                .addLine(
                        RichTextElement.atAll(),
                        RichTextElement.text(" FYI")
                )
                .build();

        FeishuPostMessage postMsg = FeishuPostMessage.builder()
                .webhookUrl(WEBHOOK_URL)
                .content(richContent)
                .build();
        assertTrue(sender.send(postMsg));
    }

    @Test
    void testSendImageMessage() {
        FeishuImageMessage imageMsg = FeishuImageMessage.builder()
                .webhookUrl(WEBHOOK_URL)
                .imageKey("v3_00v1_584d3006-2a91-4d85-8289-552446accc0g")
                .build();
        assertTrue(sender.send(imageMsg));
    }

    @Test
    void testSendShareChatMessage() {
        // This might fail if the ID is invalid, but testing the structure
        FeishuShareChatMessage shareChatMsg = FeishuShareChatMessage.builder()
                .webhookUrl(WEBHOOK_URL)
                .shareChatId("oc_1f63956ce32d1784fd29f2fa1ec01202")
                .build();
        assertTrue(sender.send(shareChatMsg));
    }

    @Test
    void testSendTemplateCardMessage() {
        FeishuTemplateCardMessage cardMsg = FeishuTemplateCardMessage.builder()
                .webhookUrl("https://open.feishu.cn/open-apis/bot/v2/hook/73696834-f1ee-4cca-8974-94f899989ee9")
                .templateId("AAqIOyaozFsoQ")
                .templateVersionName("1.0.4")
                .templateVariable(Map.of("time", "common-notice-test","exception_type","111"))
                .build();
        // This will return false if template_id is not found for this bot
        sender.send(cardMsg);
    }
}
