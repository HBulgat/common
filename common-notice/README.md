# common-notice 模块说明

本模块提供了一个统一的通知服务（Notification Service）集成门面。目前支持**飞书自定义机器人（Feishu Webhook）**和**电子邮件（Email）**两种主要的通知渠道。

通过抽象发送器（`NoticeSender`）和消息体（`NoticeMessage`），本模块允许业务代码用一套标准且简单的方式往各种不同平台上发送警报或者通知。

## 模块架构

整个模块遵循非常清晰的设计模式：多态+策略模式。
1. **统一门面 (`NoticeService`)**:
   业务中只需引入 `NoticeService` 对象，调用 `send(NoticeMessage)`，该容器类会自动将其路由给支持该 `NoticeChannel` 类型对应地具体执行类。
2. **发送器接口 (`NoticeSender`)**:
   统一动作定义。返回能够支持什么样的 `channel()` 并暴露标准的 `send()` 动作。
3. **消息基础类 (`NoticeMessage`)**:
   承载抽象属性：目标渠道 (`channel`)。

## 使用指南及类说明

### 1. 公共门面：`NoticeService`
**作用**：作为项目中发送通知请求的入口 Bean。在依赖注入的 Spring 环境下，可以将所有的 `NoticeSender` 实现类集中注册到此类内。
```java
NoticeService noticeService = new NoticeService(List.of(
    new FeishuNoticeSender(),
    new EmailNoticeSender(...) 
));

// 使用时只需要 send，底层自动路由
noticeService.send(new FeishuTextMessage(...));
```

### 2. 飞书(Feishu)支持
飞书模块主要解决通过其丰富的群组机器人 Webhook URL 反向推送多种展示格式。

#### a. `FeishuNoticeSender`
**作用**: 基于 `OkHttp` 和 `Jackson` 实现的 HTTP 通信发送器。它负责把 `FeishuMessage` 多态消息树转为实际的 JSON payload 并 POST 到目标接口。它能完美兼容绝大部分飞书标准 `msg_type` 的鉴权与结果判定。

#### b. 各种飞书消息体及其对应类
以下类都继承自 `FeishuMessage`。调用时只需组装业务数据并设置自定义的 Webhook 即可：

- **`FeishuTextMessage`**：用于发送纯文本通知。内置了静态小工具如 `atAll()` 以及 `atUser()` 来实现群组内 @ 人功能。
- **`FeishuPostMessage`**：用于发送富文本消息。
  - 需要配合 `RichTextContent`（负责管理段落和国际化标题）和 `RichTextElement` （提供如文本、超链接、@对象、图片的装配工厂方法）。
  - 例如构建高亮警告报表时非常适用。
- **`FeishuImageMessage`**：发送在飞书中早已上传过的静态图片。需要提供通过飞书上传接口获取的 `imageKey`。
- **`FeishuCardMessage`**：向接口发送纯原生的飞书 JSON 卡片 (Map)。可以兼容所有飞书卡片老旧协议。
- **`FeishuTemplateCardMessage`**：**（推荐）** 发送现代飞书卡片搭建工具做好的交互卡片模板。业务代码不需要再拼接繁琐的 JSON，只需传入 `templateId` 加上绑定的模板变量 (`templateVariable`) 字典即可发送好看规范的警报单。
- **`FeishuShareChatMessage`**：将群聊群名片（使用群聊 ID）推送出去，用于群组引流或提示切换频道。

### 3. 电子邮件(Email)支持
一套基于 JavaMail 标准 API `javax.mail` 构建的基础邮件发送层。

- **`EmailNoticeSender`**：初始化时需要注入配置信息（MailProperties 以及用户名/密码信息）。核心 `send` 方法会将邮件包装为带主题内容的支持多接收者的 `MimeMessage`，并通过 SMTP 发送。
- **`EmailMessage`**：非常直接的数据装载体：`to`（接收方 List）、`subject`（主题）、`body`（正文文本内容），并自带其渠道定义。
