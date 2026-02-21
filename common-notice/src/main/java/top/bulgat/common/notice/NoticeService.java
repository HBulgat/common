package top.bulgat.common.notice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notice service facade.
 * <p>
 * Routes messages to the correct {@link NoticeSender} implementation by channel.
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
     * Send a notice message via the corresponding channel sender.
     *
     * @param message the message to send
     * @return true if sent successfully
     * @throws IllegalArgumentException if no sender registered for the channel
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
