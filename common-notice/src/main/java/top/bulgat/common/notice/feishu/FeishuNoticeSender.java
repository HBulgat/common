package top.bulgat.common.notice.feishu;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bulgat.common.notice.NoticeChannel;
import top.bulgat.common.notice.NoticeMessage;
import top.bulgat.common.notice.NoticeSender;

import java.io.IOException;
import java.util.Map;

/**
 * 飞书自定义机器人 Webhook 发送器。
 * <p>
 * 通过委托 {@link FeishuMessage} 构建载荷来支持各种消息类型。
 */
public class FeishuNoticeSender implements NoticeSender {

    private static final Logger log = LoggerFactory.getLogger(FeishuNoticeSender.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public FeishuNoticeSender() {
        this(new OkHttpClient(), new ObjectMapper());
    }

    public FeishuNoticeSender(OkHttpClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public NoticeChannel channel() {
        return NoticeChannel.FEISHU;
    }

    @Override
    public boolean send(NoticeMessage message) {
        if (!(message instanceof FeishuMessage feishuMsg)) {
            throw new IllegalArgumentException("Expected FeishuMessage, got: " + message.getClass().getSimpleName());
        }
        try {
            Map<String, Object> body = feishuMsg.toPayload();
            String json = objectMapper.writeValueAsString(body);

            Request request = new Request.Builder()
                    .url(feishuMsg.getWebhookUrl())
                    .post(RequestBody.create(json, JSON_MEDIA_TYPE))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String respBody = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    log.warn("Feishu webhook HTTP {}: {}", response.code(), respBody);
                    return false;
                }
                
                // 飞书成功时返回 {"code":0,"msg":"success"}
                // 我们检查 "code":0 或 "StatusCode":0（v3 机器人与 v2 webhook 可能不同）
                if (respBody.contains("\"code\":0") || respBody.contains("\"StatusCode\":0")) {
                    return true;
                }
                log.warn("Feishu webhook returned error: {}", respBody);
                return false;
            }
        } catch (IOException e) {
            log.error("Failed to send Feishu notice: {}", e.getMessage(), e);
            return false;
        }
    }


}
