package top.bulgat.common.notice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知服务门面。
 * <p>
 * 按渠道将消息路由到正确的 {@link NoticeSender} 实现。
 *
 * <pre>
 * NoticeService service = new NoticeService(List.of(
 *     new FeishuNoticeSender(),
 *     new DingtalkNoticeSender(),
 *     new EmailNoticeSender(mailProps)
 * ));
 * service.send(message);
 * </pre>
 */
public class NoticeService {

    private static final Logger log = LoggerFactory.getLogger(NoticeService.class);

    private final Map<NoticeChannel, NoticeSender> senderMap = new HashMap<>();

    public NoticeService(List<NoticeSender> senders) {
        for (NoticeSender sender : senders) {
            senderMap.put(sender.channel(), sender);
        }
    }

    /**
     * 通过对应的渠道发送器发送通知消息。
     *
     * @param message 要发送的消息
     * @return 如果发送成功则返回 true
     * @throws IllegalArgumentException 如果该渠道没有注册发送器
     */
    public boolean send(NoticeMessage message) {
        NoticeChannel channel = message.getChannel();
        NoticeSender sender = senderMap.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException("No sender registered for channel: " + channel);
        }
        try {
            return sender.send(message);
        } catch (Exception e) {
            log.error("Failed to send notice via {}: {}", channel, e.getMessage(), e);
            return false;
        }
    }
}
