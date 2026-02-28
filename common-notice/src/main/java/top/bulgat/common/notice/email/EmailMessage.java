package top.bulgat.common.notice.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import top.bulgat.common.notice.NoticeChannel;
import top.bulgat.common.notice.NoticeMessage;

import java.util.List;

/**
 * 电子邮件通知消息。
 *
 * <pre>
 * EmailMessage.builder()
 *     .subject("Alert")
 *     .body("Something happened")
 *     .to(List.of("admin@example.com"))
 *     .build();
 * </pre>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmailMessage extends NoticeMessage {

    private String subject;

    private String body;

    /**
     * 收件人电子邮件地址。
     */
    private List<String> to;

    @Override
    public NoticeChannel getChannel() {
        return NoticeChannel.EMAIL;
    }
}
