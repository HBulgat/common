<div align="center">

# 🚀 Bulgat Common Framework 🚀

*打造企业级微服务与单体应用的最强基石* ✨

[![Java](https://img.shields.io/badge/Java-17-ed8b00?style=for-the-badge&logo=java&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](#)
[![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](#)

</div>

---

`common` 是一个基于 **Java 17** 和 **Spring Boot 3** 构建的💎 **企业级通用基础类库** 💎。
它旨在为微服务架构和单体应用提供**开箱即用**的底层能力支持。底层深度对核心基础组件进行了封装，提供了标准化的错误处理、多类型的消息通知能力、Spring Boot 无缝集成的增强中间件，以及一套**生产级别**的防御性缓存架构（防穿透、击穿、雪崩）。🛡️

---

## 📑 目录 (Table of Contents)

- [🧩 模块架构及依赖关系](#-模块架构及依赖关系)
- [🔥 核心模块深度解析](#-核心模块深度解析)
  - [📦 1. `common-base` (基础通用与领域模型)](#-1-common-base-基础通用与领域模型模块)
  - [⚙️ 2. `common-springboot-middleware` (Spring Boot 增强中间件)](#️-2-common-springboot-middleware-spring-boot-增强中间件)
  - [📨 3. `common-notice` (多渠道消息通知)](#-3-common-notice-多渠道消息通知模块)
  - [🛡️ 4. `common-cache` (高级防御性缓存架构)](#️-4-common-cache-高级防御性缓存架构)
- [⚡ 快速开始与集成指南](#-快速开始与集成指南)
- [🛠️ 技术栈拾遗](#️-技术栈拾遗)

---

## 🧩 模块架构及依赖关系

项目的四大核心模块在设计上保持了高度的**职责单一**和**分层隔离结构**，宛如一套精密的齿轮系统：⚙️

*   🧱 **`common-base`**： 整个生态的**绝对基石**，**0 侵入**，**不依赖** Spring 容器，仅提供最纯粹的 Java 抽象和高并发工具。
*   🚀 **`common-springboot-middleware`**： 紧密依赖 `common-base` 并强绑定 Spring Boot 生态，负责基于 AOP 和 Filter 将 base 层定义的能力（如异常处理、TraceId）在你的项目中**全自动化落地**。
*   📢 **`common-notice`**： 建立在 `common-base` 的契约之上，对外完美屏蔽诸如飞书、邮件等第三方通知 API 的繁琐细节。
*   🏰 **`common-cache`**： 一套独立且完整的**抗核爆级**缓存组件生态，将底层缓存架构武装到牙齿！

---

## 🔥 核心模块深度解析

### 📦 1. `common-base` (基础通用与领域模型模块)
> *极其轻量，随处安放，万物起源。* 🌌

本模块被设计为**绝对轻量化**，可以在任何 Java 项目中无脑引入，提供企业研发团队的统一标准定义：

*   🎯 **统一数据响应结构 (`Result` / `PageResult`)**
    *   定义了全站标准化的 Web API 响应体结构，如 `Result.success()` 与 `Result.fail()`。
    *   通过统一响应码（基于 `ErrorCode` 枚举）彻底抹平微服务间的跨度通信壁垒。
*   🛑 **全局异常标准化 (`BizException`)**
    *   定义了业务侧检查异常的制高点。任何不符合业务预期的校验结果都应抛出 `BizException`，干脆斩断代码执行流。
*   🔗 **分布式 TraceId 与跨线程透传 (`ThreadContext` / `RunnableWrapper`)**
    *   极简的 `ThreadLocal` 存储全链路标识 `traceId`。
    *   为应对异步并发池造成的 MDC 上下文断层，打造了 `RunnableWrapper` 进行**硬核**上下文状态全量接力。
*   🆔 **分布式唯一 ID 生成引擎 (`IdGenerator`)**
    *   内置工业级 `SnowflakeIdGenerator` (雪花算法：时段+机器位+序列号模式，单机抗万并发)。
    *   兼容传统 `UuidIdGenerator` 方案。

### ⚙️ 2. `common-springboot-middleware` (Spring Boot 增强中间件)
> *零配置，全自动，即插即用的魔法。* 🪄

依托 Spring Boot 强大的 SPI 和自动装配机制 (`spring-boot-autoconfigure`) 运转。**引即用，用即爽！**

*   🛡️ **全局异常收口黑洞 (`GlobalExceptionHandler`)**
    *   利用 `@RestControllerAdvice` 兜底所有 Controller 外泄异常。
    *   将 `BizException` 优雅降级为 HTTP 200 代码级 JSON。
    *   全自动拦截 `@Valid` 并组装友好的 `400 Bad Request` 中文警告。
    *   对未知致命异常 (Throwable) 完美伪装为 `500` 系统繁忙，严防堆栈侧漏。
*   👁️ **全天候可观测性自动注入 (`TraceIdFilter` & `MdcAsyncConfig`)**
    *   `TraceIdFilter` 拦截所有入口流量，秒级推入 SLF4J 的 MDC。
    *   **黑科技：** 内部重写 Spring 默认异步线程池，即便是 `@Async` 的汪洋大海，TraceId 也能原封不动跟进，ELK 日志收集从此天衣无缝！
*   📝 **Web 流量黑匣子自动打点 (`RequestLoggingFilter`)**
    *   基于 AOP 无感监控所有 HTTP Flow。精准记录出入参快照与毫秒耗时，为应用行为刻录不可磨灭的数字轨迹。

### 📨 3. `common-notice` (多渠道消息通知模块)
> *一处调用，触达全网。* 🌐

采用高级面向接口编程及门面模式 (Facade Pattern) 打造。

*   🚪 **多端智能路由门面 (`NoticeService`)**
    *   全链路告警及业务推送的唯一总阀。只需极简调用 `noticeService.send(NoticeMessage)`，系统内部即可依靠 `channel()` 多态指纹**精准打击**目标底层引擎。
*   🤖 **Feishu 飞书企业级通道深度赋能 (`FeishuNoticeSender`)**
    *   告别简陋的纯文本！内部基于 `OkHttp` 狂暴封装构建了飞书的全矩阵卡片能力：
        *   📊 `FeishuCardMessage`: 骨架屏级高级排版
        *   🖼️ `FeishuImageMessage`: 高清图片直通车
        *   🎨 富文本嵌套积木 (`RichTextContent` 及 `RichTextElement`)
        *   🤝 群组交互动作无缝分享
*   📧 **Mail 经典邮件通道 (`EmailNoticeSender`)**
    *   利用 `javax.mail` 底层构建的经典通知方式，零门槛光速接入。

### 🛡️ 4. `common-cache` (高级防御性缓存架构)
> *高并发流量面前的叹息之墙！* 🏰

该模块堪称整个框架的**巅峰之作**，它不仅是存取的 API，更是抵抗乃至免疫超高并发扫库流量的**末日堡垒** (`CacheTemplate`)。

*   🚫 **防缓存穿透：双重堡垒防御 (Null-Marker & Bloom Filter 级联策略)**
    *   **兜底防线**：`getWithPassThroughNullMarker` 会识别并代理底层无效拉取。当数据库真正 Miss 时，光速挖出一个短暂的**空值黑洞** (`properties.getNullValueTtl()`)，无情吞噬后续的穿透攻击。
    *   **前置护盾**：集成基于 Guava 的**极速内存布隆** (`LocalBloomFilterOps`) 和基于 Redisson 的**分布式布隆** (`RedisBloomFilterOps`)，在请求碰到底层前就将其蒸发。
*   ⏳ **防缓存击穿：热点永生机制 (Logical Expiry)**
    *   **大促杀器**：`getWithLogicalExpiry` 专为**超高热频打点 Key** 订制。剥离原生 TTL 强行删除机制，在数据内植入“时空胶囊” (`CacheData<V>`) 逻辑标期。
    *   **核心魔法**：时辰一到，立刻用旧数据光速“糊弄”前端响应的同时，后台 `rebuildExecutor` 线程暗度陈仓，全异步杀去 DB 换回新鲜血液，实现**绝对零延迟、绝无锁阻塞**！
*   🎲 **防缓存雪崩：量子概率抖动 (Jitter Strategy)**
    *   针对批量被动写入，底层引擎会通过 `jitter()` 注入**混沌变量**。利用 `jitterFactor` 对原始 TTL 进行正态无序扩散，彻底粉碎因时间片共振引发的数据库大雪崩。
*   🚄 **反重力批量聚合 (`multiGet*` I/O 压缩)**
    *   无情压榨网络 RTT 极限！利用 Redis 内核命令 `multiGet` 进行重火力流水线覆盖。
    *   无论是对付防穿透 (`multiGetWithPassThroughNullMarker`) 还是逻辑过期 (`multiGetWithLogicalExpiry`)，皆可一揽子吞吐数据，仅对漏网之鱼采用 `multiLoad()` 实施定点大批量打击补货。

---

## ⚡ 快速开始与集成指南

**1. 🌟 服务化全景引入** *(解锁 MVC 全场景异常护航、统一出入参、全链路天启追踪)*：
```xml
<dependency>
    <groupId>top.bulgat</groupId>
    <artifactId>common-springboot-middleware</artifactId>
    <version>1.0.0</version> <!-- 请替换为您的实际版本 -->
</dependency>
```

**2. 💥 高级缓存装甲挂载** *(解锁防崩溃/击穿/雪崩终极防御圈)*：
```xml
<dependency>
    <groupId>top.bulgat</groupId>
    <artifactId>common-cache</artifactId>
    <version>1.0.0</version> <!-- 请替换为您的实际版本 -->
</dependency>
```
> 💡 **大师级实践：** 在您的业务逻辑中，只需高傲地继承或实现 `CacheLoader` 接口，制定好您的数据源契约（如 MyBatis），随后将所有的读取重任全盘外包给万能的 `CacheTemplate` 代理人！

---

## 🛠️ 技术栈拾遗
构建这一钢铁洪流所依赖的核心能源：

*   ☕ **核心动力**: Java 17
*   🍃 **框架骨架**: Spring Boot 3.3.0
*   🧬 **分布枢纽**: `org.redisson` (Redisson 分布式布隆与锁控)
*   ⚡ **极速引擎**: `com.google.guava` (本地纳秒级缓存与黑洞拦截)
*   🚀 **网络超导**: `com.squareup.okhttp3` (强劲的外部 HTTP 异构通信引擎)
*   📊 **全息日志**: `net.logstash.logback` (100% 适配 ELK 规范的结构化编码器)
*   📦 **序列化解析**: Jackson 2.17.0

---
<div align="center">
  <sub>Built with ❤️ by Bulgat</sub>
</div>
