# Common (Bulgat) 底层基础框架项目

`common` 是一个基于 **Java 17** 和 **Spring Boot 3** 构建的通用基础构件库。
它将企业级日常开发中常见但易碎的非功能性需求（缓存、异步、通知、雪花ID、异常规整等）抽象封装成多个松耦合的模块。

旨在帮助开发者能够**开箱即用**、**零/低配置**地武装微服务，从而专注于纯粹的业务逻辑。

## 整体架构与模块划分

项目按照职责切分为四个完全解耦的 Module，分别承载不同层级的核心能力。
通常情况下，业务只需要引入 `common-springboot-middleware` 即可获得极大部分增强特性，并根据特定需求按需补充引入 `common-notice` 或 `common-cache`。

---

### 1. `common-base` (核心基础库)
**定位**：整个项目的最底层基石。零侵入，不依赖任何 Spring 上下文。

**主要内容：**
- **常量定义** (`CommonConstants`)：维护跨应用约定的统一特殊值。
- **公用枚举** (`ErrorCode`)：包含全局统一响应错误状态码规范。
- **统一响应模型** (`Result<T>` / `PageResult<T>`)：标准化的 API RestFul 响应数据结构。
- **自定义异常基类** (`BizException`)：用于业务代码主动抛出错误以切断执行流，配合外层进行受控报错。
- **唯一 ID 生成引擎** (`IdGenerator`)：内置高性能 `SnowflakeIdGenerator` (雪花算法) 和兼容的 `UuidIdGenerator`。
- **各类安全与并发工具**：如 `ThreadContext`，用于承载全链路透传信息；通过 `RunnableWrapper` 实现跨线程透传。

👉 [查看 common-base 详细文档](./common-base/README.md)

---

### 2. `common-springboot-middleware` (Spring 增强中间件)
**定位**：向 Spring Boot 无缝挂载强大特性的 Starter。

**主要内容：**
本模块通过自动装配机制 (`AutoConfiguration`) 提供了一批企业级基础增强环境组件：
- **`GlobalExceptionHandler`**：全局拦截 Spring Web 请求抛出的任何异常。将其转化为标准的 `Result` JSON 并严防堆栈侧漏（未知致命异常伪装为 500）。
- **零配置分布式 TraceId 链路追踪体系**：通过高优先级过滤器 `TraceIdFilter` 派发/继承 `traceId` 并塞入 `MDC` 给 Logback。配备 `MdcAsyncConfig` 保障该 ID 能穿透 `@Async("mdcTaskExecutor")` 异步线程不出错。
- **`RequestLoggingFilter`**：全天候可观测的 Web 流量黑匣子，基于 AOP 精准记录出入参快照与毫秒耗时。
- **`logback-spring.xml`**：内建日志分级切割体系。开发时终端彩色高亮；生产时异步写 JSON (`net.logstash.logback`) 供 ELK 直连抓取。
- **`JsonConfig`**：解决双端交互通病，例如强制 ISO-8601 日期格式，将 `Long` 自动转成 `String` 防止前端 JS 精度丢失。

� [查看 common-springboot-middleware 详细文档](./common-springboot-middleware/README.md)

---

### 3. `common-cache` (企业缓存组件)
**定位**：对抗高并发缓存穿透、击穿、雪崩的解决方案抽象层。

**主要内容：**
- **统一调用外观**：**`CacheTemplate`**，屏蔽底部复杂设计，外部只需使用 `getOrElse` 即可实现安全读取。
- **防缓存穿透 (Null Marker & Bloom Filter)**：兜底时使用自带短时 TTL 的空值标记（Null Marker）应对无效拉取；提供本地（Guava）或分布式（Redisson）布隆过滤器进行前置护盾拦截。
- **防缓存击穿 (Logical Expiry)**：专为超高频热点 key 设计的逻辑过期策略。时辰一到优先返回旧数据，并在后台异步线程 (`rebuildExecutor`) 默默刷新 DB，实现绝对零延迟。
- **防缓存雪崩 (TTL Jitter)**：支持配置布尔开关或因子，使 TTL 产生随机浮动（如 ±20%），分散过期时间。
- **高性能批量聚合 (`multiGet*`)**：支持基于 Redis 原生能力的流水线式批量处理穿透及逻辑过期策略的数据，极限压榨 I/O。

👉 [查看 common-cache 详细文档](./common-cache/README.md)

---

### 4. `common-notice` (统一消息通知频道)
**定位**：发送对外警告或不同渠道消息的多态集成网关。

**主要内容：**
利用抽象发送器和标准消息体的策略模式设计，统一应用向外部平台“喊话”的动作。
- **核心路由器：`NoticeService`**，仅依靠此入口，塞入一个目标渠道体，自动匹配信道将信息投递。
- **Feishu (飞书机器人 Webhook)**：提供强大的构建与装配工具（纯文本/富文本/图片/JSON卡片/模板卡片/分享群名片等）。原生兼容 `@All` 或 `@user` 支持。
- **Email (SMTP 邮箱)**：提供了标准化的主题及信函内容的 JavaMail API 二次封装能力。

👉 [查看 common-notice 详细文档](./common-notice/README.md)

---

## 快速集成

在您的业务工程中引入需要的底层模块即可：

```xml
<dependency>
    <groupId>top.bulgat</groupId>
    <artifactId>common-springboot-middleware</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>top.bulgat</groupId>
    <artifactId>common-cache</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
