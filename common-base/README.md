# common-base 模块说明

本模块提供项目的公共基础能力，包含常量定义、统一返回结果包装、全局异常处理、ID 生成器、线程上下文传递、时间工具及其他常用工具类。

以下为本模块中各个类的详细使用说明：

## 1. 常量与枚举
### `CommonConstants` (常量池)
提供项目中常用的公共常量，如：
- 分页参数：`DEFAULT_PAGE_NUM`, `DEFAULT_PAGE_SIZE`, `MAX_PAGE_SIZE`
- 分隔符：`EMPTY`, `COMMA`, `COLON`, `DOT`, `SLASH`
- 布尔标识：`YES`, `NO`
- 日期格式：`DATE_FORMAT`, `DATETIME_FORMAT`
**使用示例**：
```java
int pageNum = CommonConstants.DEFAULT_PAGE_NUM;
```

### `ErrorCode` (错误码枚举)
定义了系统中常见的错误代码和提示信息，如 `SUCCESS(200)`, `BAD_REQUEST(400)` 等。
**使用示例**：
```java
if (param == null) {
    return Result.fail(ErrorCode.PARAM_ERROR);
}
```

## 2. 全局异常
### `BizException` (业务异常)
用于在业务逻辑层面抛出的异常。它支持通过错误码 (`ErrorCode`) 或者自定义错误消息进行构造。
**使用示例**：
```java
if (user == null) {
    throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
}
```

## 3. 请求和响应模型
### `Result<T>` (统一响应包装类)
统一接口返回数据结构。提供多种静态工厂方法，如 `Result.success()`, `Result.fail()` 等。
**使用示例**：
```java
@GetMapping("/user")
public Result<User> getUser() {
    User user = userService.getUser();
    return Result.success(user);
}
```

### `PageResult<T>` (分页响应包装类)
用于统一分页返回的数据结构。包含当前页码 `current`、页面大小 `size`、总记录数 `total` 以及当前页的数据列表 `list`。
**使用示例**：
```java
return Result.success(PageResult.of(1, 10, 100L, userList));
```

## 4. ID 生成器
### `IdGenerator` (ID 生成器接口)
定义了生成 long 类型 ID (`nextId()`) 和 String 类型 ID (`nextIdStr()`) 的方法接口。

### `SnowflakeIdGenerator` (雪花算法 ID 生成器)
生成 64 位且按时间递增的分布式唯一长整型 ID。构造时需提供 workerId 和 datacenterId。
**使用示例**：
```java
IdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);
long unifiedId = idGenerator.nextId();
```

### `UuidIdGenerator` (UUID 生成器)
基于 `java.util.UUID` 生成无连字符的小写字符串 ID。注意它不支持 `nextId()` 生成 long 类型 ID。
**使用示例**：
```java
IdGenerator idGenerator = new UuidIdGenerator();
String strId = idGenerator.nextIdStr();
```

## 5. 线程与并发
### `ThreadContext` (线程上下文)
利用 `ThreadLocal` 存储当前线程的 `traceId`，常用于日志或请求的链路追踪。
**使用示例**：
```java
ThreadContext.setTraceId("trace-12345");
String currentTraceId = ThreadContext.getTraceId();
ThreadContext.clear();
```

### `RunnableWrapper` (带上下文传递的 Runnable 包装类)
在跨线程派发任务（如往线程池提交任务）时，能够自动从提交线程捕获 `traceId`，并在工作线程中恢复上下文。
**使用示例**：
```java
ThreadContext.setTraceId("ctx-001");
executor.submit(RunnableWrapper.of(() -> {
    // 这里的 ThreadContext.getTraceId() 会输出 ctx-001
    System.out.println(ThreadContext.getTraceId()); 
}));
```

## 6. 工具类
### `SystemClock` (高性能系统时钟)
通过后台单线程定时刷新缓存的 `System.currentTimeMillis()`，避免高并发下高频获取系统时间的性能损耗。
**使用示例**：
```java
long timestamp = SystemClock.millisClock().now();
```

### `DateUtils` (日期时间工具类)
基于 Java 8 `java.time` API 的常用日期和时间处理工具（处理 `LocalDateTime` 等），支持格式化转化、起止时间计算。
**使用示例**：
```java
String nowStr = DateUtils.now(); // 返回诸如 "2024-01-01 12:00:00"
LocalDateTime time = DateUtils.parseDateTime("2024-01-01 12:00:00");
```

### `JsonUtils` (JSON 工具类)
基于 Jackson 并内置处理 `java.time` 和忽略未知属性等优化配置。
**使用示例**：
```java
String jsonStr = JsonUtils.toJson(user);
User parsedUser = JsonUtils.fromJson(jsonStr, User.class);
```

### `StringUtils` (字符串工具类)
提供了判空、去重、驼峰与下划线相互转换等基础处理方法。
**使用示例**：
```java
boolean blank = StringUtils.isBlank("  "); // true
String camel = StringUtils.underscoreToCamel("user_name"); // userName
```

### `ThrowUtils` (便捷异常抛出工具类)
代码简洁化工具，可以在条件满足或对象为 null 时直接抛出对应的业务异常，减少大量 if 判断样板代码。
**使用示例**：
```java
ThrowUtils.throwIfNull(user, ErrorCode.NOT_FOUND, "该用户不存在");
ThrowUtils.throwIfBlank(user.getName(), ErrorCode.PARAM_ERROR);
ThrowUtils.throwIf(count < 0, ErrorCode.BAD_REQUEST, "数值不能为负");
```
