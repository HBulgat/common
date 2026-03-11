package top.bulgat.common.notice.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.bulgat.common.base.util.StringUtils;

import java.util.Properties;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailSenderMeta {
    private String host;
    private Integer port;
    private boolean auth;
    private boolean sslEnable;
    private String charset;

    public static Properties toProperties(EmailSenderMeta meta){
        if(meta==null) throw new IllegalArgumentException("meta can't be null");
        Properties properties=new Properties();
        properties.put("mail.smtp.host",meta.getHost());
        properties.put("mail.smtp.port",String.valueOf(meta.getPort()));
        properties.put("mail.smtp.auth",String.valueOf(meta.isAuth()));
        properties.put("mail.smtp.ssl.enable",String.valueOf(meta.isSslEnable()));
        properties.put("mail.mime.charset", StringUtils.isBlank(meta.getCharset())?"UTF-8":meta.getCharset());
        return properties;
    }
}
