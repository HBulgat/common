package top.bulgat.common.notice;

/**
 * 通知发送器接口。
 * <p>
 * 每个实现处理一个特定的 {@link NoticeChannel}。
 */
public interface NoticeSender {

    /**
     * 该发送器处理的渠道。
     */
    NoticeChannel channel();

    /**
     * 发送消息。
     *
     * @param message 通知消息
     * @return 如果发送成功则返回 true
     */
    boolean send(NoticeMessage message);
}
