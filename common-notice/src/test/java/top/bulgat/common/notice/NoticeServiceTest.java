package top.bulgat.common.notice;

import org.junit.jupiter.api.Test;
import top.bulgat.common.notice.feishu.FeishuTextMessage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NoticeServiceTest {

    static class FakeSender implements NoticeSender {
        boolean called = false;
        boolean shouldSucceed = true;
        private final NoticeChannel ch;

        FakeSender(NoticeChannel ch) {
            this.ch = ch;
        }

        @Override
        public NoticeChannel channel() {
            return ch;
        }

        @Override
        public boolean send(NoticeMessage message) {
            called = true;
            return shouldSucceed;
        }
    }

    @Test
    void testRouteToCorrectSender() {
        FakeSender feishu = new FakeSender(NoticeChannel.FEISHU);
        FakeSender email = new FakeSender(NoticeChannel.EMAIL);
        NoticeService service = new NoticeService(List.of(feishu, email));

        FeishuTextMessage msg = FeishuTextMessage.builder()
                .webhookUrl("http://test")
                .text("hello")
                .build();

        assertTrue(service.send(msg));
        assertTrue(feishu.called);
        assertFalse(email.called);
    }

    @Test
    void testNoSenderRegistered() {
        NoticeService service = new NoticeService(List.of());

        FeishuTextMessage msg = FeishuTextMessage.builder()
                .webhookUrl("http://test")
                .text("hello")
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.send(msg));
    }

    @Test
    void testSenderFailure() {
        FakeSender sender = new FakeSender(NoticeChannel.FEISHU);
        sender.shouldSucceed = false;
        NoticeService service = new NoticeService(List.of(sender));

        FeishuTextMessage msg = FeishuTextMessage.builder()
                .webhookUrl("http://test")
                .text("hello")
                .build();

        assertFalse(service.send(msg));
    }
}
