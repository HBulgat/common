package top.bulgat.common.notice;

/**
 * Notice sender interface.
 * <p>
 * Each implementation handles a specific {@link NoticeChannel}.
 */
public interface NoticeSender {

    /**
     * The channel this sender handles.
     */
    NoticeChannel channel();

    /**
     * Send the message.
     *
     * @param message notice message
     * @return true if sent successfully
     */
    boolean send(NoticeMessage message);
}
