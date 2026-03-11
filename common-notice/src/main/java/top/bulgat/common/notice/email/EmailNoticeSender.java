package top.bulgat.common.notice.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bulgat.common.notice.NoticeChannel;
import top.bulgat.common.notice.NoticeMessage;
import top.bulgat.common.notice.NoticeSender;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 通过 SMTP (JavaMail) 发送电子邮件通知的发送器。
 */
public class EmailNoticeSender implements NoticeSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNoticeSender.class);

    private final Session session;
    private final String from;

    public EmailNoticeSender(Properties mailProps, String username, String password) {
        this.from = username;
        this.session = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public EmailNoticeSender(EmailSenderMeta meta, String username, String password) {
        this.from = username;
        this.session = Session.getInstance(EmailSenderMeta.toProperties(meta), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public NoticeChannel channel() {
        return NoticeChannel.EMAIL;
    }

    @Override
    public boolean send(NoticeMessage message) {
        if (!(message instanceof EmailMessage emailMsg)) {
            throw new IllegalArgumentException("Expected EmailMessage, got: " + message.getClass().getSimpleName());
        }
        try {
            MimeMessage mail = new MimeMessage(session);
            mail.setFrom(new InternetAddress(from));

            if (emailMsg.getTo() != null) {
                for (String to : emailMsg.getTo()) {
                    mail.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                }
            }

            mail.setSubject(emailMsg.getSubject() != null ? emailMsg.getSubject() : "");
            mail.setText(emailMsg.getBody(), "UTF-8");

            Transport.send(mail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email notice: {}", e.getMessage(), e);
            return false;
        }
    }
}
