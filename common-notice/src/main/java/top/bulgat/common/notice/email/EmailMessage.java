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
 * Email notice message.
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
     * Recipient email addresses.
     */
    private List<String> to;

    @Override
    public NoticeChannel getChannel() {
        return NoticeChannel.EMAIL;
    }
}
