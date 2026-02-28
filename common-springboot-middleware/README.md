# common-springboot-middleware 模块说明

该模块作为基于 Spring Boot 发行版构建微服务时的底层基础设施。它包含了各类对于健壮型后台必不可少的 AutoConfiguration 组件。业务侧只需将其引入依赖树，大量的非功能性需求（如：错误规整化、日志追踪、通用反序列化）即可通过少量的 `application.yml` 甚至零配置得到强化支持。

## 模块架构与功能划分

本模块由 `MiddlewareAutoConfiguration` 统一装配。所有的核心能力都被注入为了 `@Bean` 或者 `@RestControllerAdvice` 甚至 `@Filter`。它通过 Spring `META-INF/spring...AutoConfiguration.imports` 被宿主 SpringBoot 应用自动发现并启动。

### 1. 全局异常处理 (`GlobalExceptionHandler`)
拦截整个 Servlet 容器以及 Spring Web MVC 执行过程中抛出的所有错漏，将其转化为前端或者调用者可读的标准 `Result<Void>` JSON。

- **`BizException` 拦截**：如果业务代码主动 `throw new BizException(...)` 或者通过 `ThrowUtils` 触发，说明是一个预期内的失败。它将被包装成标准的 `Result.fail(...)` 并响应 `HTTP 200`，**同时防止打印不必要的完整堆栈**。
- **校验抛出 (`MethodArgumentNotValidException`, `BindException`)**：对于挂载了 `@Valid` 或 `@Validated` 的参数对象，其如果包含未通过如 `@NotNull`, `@Min` 等验证的参数，此异常将会拦截抛出，抓取所有的 `message` 字段拼接后返回，状态码置为 `400 Bad Request`。
- **兜底未知错误 (`Throwable`)**：对于预期外的宕机异常如 NPE、SQL 错漏等，它将被拦截，向调用方模糊报错“系统繁忙”，但是会在应用后端直接将 `e.printStackTrace()` 完全打入 `LOG.error` 级别日志。状态码为 `500`。

### 2. TraceId 全链路追踪日志体系 (`TraceIdFilter` & `MdcAsyncConfig`)
用于解决分布式日志关联以及跨线程日志断层的问题。

- **`TraceIdFilter`** （最高优先级过滤器 `Oder(1)`）：
  每一次外部进入一个 HTTP 请求。过滤器会首先检查 HTTP Header `X-Trace-Id` 是否存在。如果不存在，将会为其主动生成 UUID 的 traceId 字符串。
  接着该字符串会被通过 `MDC`（供 Logback `%X{traceId}` 提取）和 `ThreadContext`（供同线程业务上下文代码显式提取）这两种手段注入。
  最后同样把该 ID 设回客户端响应包的 header，并在整个 MVC 链结束的 `finally` 阶段将其彻底由当前线程销毁，从而防范内存泄漏或者线程池污染。

- **`MdcAsyncConfig`**：
  默认提供了叫做 `taskExecutor` 的 `@Async` 支持执行器。它覆写并提供了 `TaskDecorator`。在任务通过 `@Async` 注解提交到线程池排队时，其能够主动截获主调线程里暂存的 `MDC(TraceId)`，并在工作子线程启动时重新覆盖它。借此保障所有后台异步线程打印的日志全部带有和同步控制器一模一样的前缀 TraceId。

### 3. 请求打点过滤器 (`RequestLoggingFilter`)
此过滤器不会默认开启。需在 `application.yml` 中主动打开 `common.middleware.request-log.enabled=true` 配置。
开启以后，它会拦截一切调用，并在执行完毕响应后记录极其实用的访问存根 (Access Log)：
`[POST /api/user?id=1] 200 | 150ms | traceId=xxxxxxxxxx`

### 4. Jackson 前后端标准传输配置 (`JsonConfig`)
提供针对 SpringBoot 内嵌 `ObjectMapper` 的全局重写，消除前端兼容性坑。
- 强制使用 ISO-8601 字符串替代 UNIX 时戳进行 `LocalDateTime` 和 `LocalDate` 的双向通信。
- **（重要）将 `Long` 与 `long` 的响应转为 JSON String 字符串**：由于 JavaScript 内部采用 IEEE 754 双精度对象，Long 如果超过某个大整数限度将会产生前台 ID 精度丢失（如后两位变成0）。本设置让返回前端一切 Long 都是字符串防止精度坍塌。
- 反序列化容错支持，当碰到目标实体没有任何属性绑定时不会直接导致请求解析挂掉报错 400。

### 5. 雪花 ID 生成器 (`SnowflakeIdConfig`)
基于 `common-base` 内的 `SnowflakeIdGenerator` 为当前 Spring 应用自动提供 `IdGenerator` Bean 实例。
可以基于以下设置定制机器节点属性：

```yaml
common:
  snowflake:
    worker-id: 1
    datacenter-id: 1
```

### 6. 日志输出及收集体系 (`logback-spring.xml`)
本模块内建了标准化的 Logback 日志配置（位于 `src/main/resources/logback-spring.xml`），引入该模块的 Spring Boot 项目将直接继承这一套配置。

在不同的 `spring.profiles.active` 环境下提供了不同策略：
- **`dev` 或 `local`（本地/开发环境）**：启用 `CONSOLE` 终端输出，并且高亮打印，便于开发者观察线程堆栈和 `traceId`。
- **`prod`（生产环境）**：屏蔽本地终端输出，纯异步通过 `ASYNC_FILE_JSON` 将日志写入 `/logs/{应用名}.json.log` 文件。
- 该文件每一行是一个纯 JSON 对象，里面已自带了格式化好的 `@timestamp`, `traceId`, `logger_name`, `stack_trace`。非常适合无缝采集并且丢进 Filebeat → Logstash → Elasticsearch 体系，不再需要复杂的正则匹配。
