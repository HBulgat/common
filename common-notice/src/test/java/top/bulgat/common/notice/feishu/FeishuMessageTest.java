package top.bulgat.common.notice.feishu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import top.bulgat.common.base.util.JsonUtils;

public class FeishuMessageTest {

    ObjectMapper mapper=new ObjectMapper();
    @Test
    public void testJson() throws JsonProcessingException {
        String json="{\"msg_type\" : \"text\"}";
        FeishuMessage message=mapper.readValue(json,FeishuMessage.class);
        System.out.println(message);
    }
}
