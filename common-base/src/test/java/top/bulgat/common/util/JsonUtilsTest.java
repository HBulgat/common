package top.bulgat.common.util;

import org.junit.jupiter.api.Test;
import top.bulgat.common.base.util.JsonUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void testToJson() {
        TestUser user = new TestUser("张三", 25);
        String json = JsonUtils.toJson(user);
        assertNotNull(json);
        assertTrue(json.contains("张三"));
        assertTrue(json.contains("25"));
    }

    @Test
    void testToJson_null() {
        assertNull(JsonUtils.toJson(null));
    }

    @Test
    void testFromJson() {
        String json = "{\"name\":\"张三\",\"age\":25}";
        TestUser user = JsonUtils.fromJson(json, TestUser.class);
        assertNotNull(user);
        assertEquals("张三", user.getName());
        assertEquals(25, user.getAge());
    }

    @Test
    void testFromJson_null() {
        assertNull(JsonUtils.fromJson(null, TestUser.class));
        assertNull(JsonUtils.fromJson("", TestUser.class));
    }

    @Test
    void testFromJson_ignoreUnknownProperties() {
        String json = "{\"name\":\"张三\",\"age\":25,\"extra\":\"unknown\"}";
        TestUser user = JsonUtils.fromJson(json, TestUser.class);
        assertNotNull(user);
        assertEquals("张三", user.getName());
    }

    @Test
    void testToMap() {
        String json = "{\"key1\":\"value1\",\"key2\":123}";
        Map<String, Object> map = JsonUtils.toMap(json);
        assertNotNull(map);
        assertEquals("value1", map.get("key1"));
        assertEquals(123, map.get("key2"));
    }

    @Test
    void testToList() {
        String json = "[{\"name\":\"A\",\"age\":1},{\"name\":\"B\",\"age\":2}]";
        List<TestUser> list = JsonUtils.toList(json, TestUser.class);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0).getName());
    }

    @Test
    void testConvert() {
        Map<String, Object> map = Map.of("name", "李四", "age", 30);
        TestUser user = JsonUtils.convert(map, TestUser.class);
        assertNotNull(user);
        assertEquals("李四", user.getName());
        assertEquals(30, user.getAge());
    }

    @Test
    void testPrettyJson() {
        TestUser user = new TestUser("test", 1);
        String pretty = JsonUtils.toPrettyJson(user);
        assertNotNull(pretty);
        assertTrue(pretty.contains("\n"));
    }

    // 测试用内部类
    static class TestUser {
        private String name;
        private int age;

        public TestUser() {
        }

        public TestUser(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
}
