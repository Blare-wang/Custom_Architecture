# 基于Spring boot 快速构建maven骨架
三个统一：统一日志、统一异常、统一响应返回

## 一、日志配置
Spring Boot采用yml的方式配置 Log4j2 日志文件：
- 具有更快的执行速度
- 异步性能
- 自动加载日志配置文件的功能，且发生改变时不会丢失任何日志事件
- 死锁问题的解决
### 1.1 Maven 依赖 pom.xml配置
- 去掉默认日志，加载别的日志，spring boot提供log4j2的解决方案，如下配置
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <!-- 切换log4j2日志读取，移除默认日志配置 -->
        <exclusions>
            <exclusion>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-logging</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <!-- 配置 log4j2 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-log4j2</artifactId>
    </dependency>
    <!-- 加上这个才能辨认到log4j2.yml文件 -->
    <dependency>
        <groupId>com.fasterxml.jackson.dataformat</groupId>
        <artifactId>jackson-dataformat-yaml</artifactId>
    </dependency>
</dependencies>
```
### 1.2 配置文件添加log4j2.yml
```yaml
logging:
  config: classpath:log/log4j2-dev.yml 
```

### 1.3 引入配置文件
[Log4j2官网](https://logging.apache.org/log4j/2.x/manual/appenders.html)

[控制台颜色显示](http://logging.apache.org/log4j/2.x/manual/layouts.html#enable-jansi)
：IDEA中，点击右上角->Edit Configurations，在VM options中添加 -Dlog4j.skipJansi=false

```yaml
# 共有8个级别，按照从低到高为：ALL < TRACE < DEBUG < INFO < WARN < ERROR < FATAL < OFF。
# intLevel值依次为0,100,200,300,400,500,600,700
# intLevel 值越小，级别越高
Configuration:
  # 日志框架本身的输出日志级别
  status: warn
  # 自动加载配置文件的间隔时间，单位：秒，不低于5秒
  monitorInterval: 5
  # packages: com.smartadmin.config.log.plugin
  # 定义全局变量，引入到后面配置中，以List的方式配置
  Properties:
    # 缺省配置（用于开发环境）
    Property:
      # 输出文件路径
      - name: log.path
        value: log
      # 项目名称
      - name: project.name
        value: custom_archetype
      # 默认日志输出格式
      - name: log.pattern
        value: "%d{yyyy-MM-dd HH:mm:ss.SSS} -%5p ${PID:-} [%15.15t] %-30.30C{1.} : %m%n"
        # value: "%d %highlight{%-5level}{ERROR=Bright RED, WARN=Bright Yellow, INFO=Bright Green, DEBUG=Bright Cyan, TRACE=Bright White} %style{[%t]}{bright,magenta} %style{%c{1.}.%M(%L)}{cyan}: %msg%n"
  Appenders:
    # 输出到控制台
    Console:
      name: CONSOLE
      target: SYSTEM_OUT
      # 指定日志级别 控制台只输出level及以上级别的信息，其他的直接拒绝
      # onMatch：和level级别匹配， onMismatch：和 level级别不匹配
      ThresholdFilter:
        level: TRACE
        onMatch: ACCEPT
        onMismatch: DENY
      # 日志消息格式
      PatternLayout:
        pattern: ${log.pattern}
        charset: UTF-8
    # 输出到文件，超过128MB归档
    RollingFile:
      # 日志模块名称
      - name: ROLLING_FILE
        ignoreExceptions: false
        # 输出文件的地址
        fileName: ${log.path}/${project.name}.log
        # 文件生成规则
        filePattern: "${log.path}/$${date:yyyy-MM}/${project.name}-%d{yyyy-MM-dd}-%i.log.gz"
        # 日志格式
        PatternLayout:
          pattern: ${log.pattern}
          charset: UTF-8
        Filters:
          # 一定要先去除不接受的日志级别，然后获取需要接受的日志级别
          ThresholdFilter:
            # 日志级别
            - level: ERROR
              onMatch: DENY
              onMismatch: NEUTRAL
            - level: INFO
              onMatch: ACCEPT
              onMismatch: DENY
            - level: DEBUG
              onMatch: ACCEPT
              onMismatch: DENY
        # 日志拆分规则
        Policies:
          # 日志拆分规则
          SizeBasedTriggeringPolicy:
            size: 128MB
          # 按天分类
          TimeBasedTriggeringPolicy:
            modulate: true
            interval: 1
        # 单目录下，文件最多100个，超过会删除最早之前的
        DefaultRolloverStrategy:
          max: 10
  Loggers:
    Root:
      # root的级别为info，如果为debug的话，输出的内容太多
      level: INFO
      includeLocation: true
      AppenderRef:
        - ref: CONSOLE
        - ref: ROLLING_FILE
    Logger:
      # 监听具体包下面的日志
      - name: com.itblare
        # 去除重复的log
        additivity: false
        level: TRACE
        AppenderRef:
          #复数加上
          - ref: CONSOLE
          - ref: ROLLING_FILE
      - name: org.springframework
        level: DEBUG
```

## 二、统一异常处理
### 2.1 自定义异常
继承RuntimeException，自定义自己的异常类
```java
public class BaseException extends RuntimeException {

    private String code;
    private String message;

    public BaseException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(String code, String message, Throwable ex) {
        super(message, ex);
        this.code = code;
        this.message = message;
    }

    public BaseException(Throwable exception) {
        super(exception);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
```

### 2.2 全局异常统一处理（Global unified exception processing）
基于@ControllerAdvice和@ExceptionHandler做全局异常统一处理
```java
@RestControllerAdvice // 等价于@ControllerAdvice+@ResponseBody
public class UnifiedExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(UnifiedExceptionHandler.class);

  /**
   * 处理未捕获的Exception
   *
   * @param ex 异常
   * @author Blare
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseDataWrapper<Object> handleException(Exception ex) {
    logger.error(ex.getMessage(), ex);
    return ResponseDataFactory.wrapper(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  /**
   * 处理未捕获的RuntimeException
   *
   * @param ex 运行时异常
   * @author Blare
   */
  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseDataWrapper<?> handleRuntimeException(RuntimeException ex) {
    logger.error(ex.getMessage(), ex);
    return ResponseDataFactory.wrapper(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  /**
   * 处理自定义异常
   *
   * @param ex 自定义异常
   * @author Blare
   */
  @ExceptionHandler(BaseException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseDataWrapper<Object> APIExceptionHandler(BaseException ex) {
    logger.error("api异常");
    return ResponseDataFactory.errorCodeMessage(ex.getStatus(), ex.getMessage());
  }
}
```

后续可配合ResponseBodyAdvice接口，做增强处理

## 三、接口统一响应处理（Interface unified response processing）
### 3.1 常用HTTP请求指令返回状态（详见：org.springframework.http）
- 200（成功） 服务器已成功处理了请求。
- 400（错误请求） 服务器不理解请求的语法。
- 401（身份验证错误） 此页要求授权。您可能不希望将此网页纳入索引。
- 403（禁止） 服务器拒绝请求。
- 404（未找到） 服务器找不到请求的网页。例如，对于服务器上不存在的网页经常会返回此代码。
- 405（方法禁用） 禁用请求中指定的方法。 
- 500（服务器内部错误） 服务器遇到错误，无法完成请求。
- 502（错误网关） 服务器作为网关或代理，从上游服务器收到了无效的响应。
- 504（网关超时） 服务器作为网关或代理，未及时从上游服务器接收请求。
- 505（HTTP 版本不受支持） 服务器不支持请求中所使用的 HTTP 协议版本。

### 3.2 统一响应处理切换
- 自定义统一响应切换注解
```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseWrapper {
}
```
- 拦截器处理
```java
@Component
public class ResponseWrapperInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if (handler instanceof final HandlerMethod handlerMethod) {
            final Class<?> classType = handlerMethod.getBeanType();
            final Method method = handlerMethod.getMethod();
            // 确保当前类是RestController，不然不进行封装
            if (classType.isAnnotationPresent(RestController.class)) {

                // 从方法上到类上再到超类上查看是否有指定注解
                boolean hasResponseResultAnnotation = method.isAnnotationPresent(ResponseWrapper.class);

                if (!hasResponseResultAnnotation) {
                    // 此类上是否由注释
                    hasResponseResultAnnotation = hasAnnotationWithParent(classType);
                }

                if (!hasResponseResultAnnotation) {
                    // 方法和当前类上无ResponseResult注解，那么查看其实现接口是否由ResponseResult注解
                    final Class<?>[] interfaces = classType.getInterfaces();
                    if (interfaces.length > 0) {
                        for (Class<?> anInterface : interfaces) {
                            hasResponseResultAnnotation = hasAnnotationWithParent(anInterface);
                            if (hasResponseResultAnnotation) {
                                break;
                            }
                        }
                    }
                }
                request.setAttribute(CommonConstant.RESPONSE_WRAPPER_ANN, hasResponseResultAnnotation);
            }
        }

        return true;
    }

    /**
     * 检测当前类/接口及其父类是否包含该注解
     *
     * @param declaringClass 检测的类
     * @return {@link boolean}
     * @author Blare
     */
    private boolean hasAnnotationWithParent(Class<?> declaringClass) {

        boolean hasAnnotation = declaringClass.isAnnotationPresent(ResponseWrapper.class);
        while (!hasAnnotation) {
            final String className = declaringClass.getName();
            //已经是最顶层父类
            if ("java.lang.Object".equals(className)) {
                break;
            }

            declaringClass = declaringClass.getSuperclass();// 父类
            if (Objects.isNull(declaringClass)) {
                break;
            }
            hasAnnotation = declaringClass.isAnnotationPresent(ResponseWrapper.class);
        }
        return hasAnnotation;
    }

}
```

### 3.3 统一数据封装
- 结果包装类
```java
public class BaseResponseWrapper<T> implements Serializable {

    private int status;
    private String message;


    protected BaseResponseWrapper(HttpStatus status) {
        this.status = status.value();
        this.message = status.name();
    }

    protected BaseResponseWrapper(int status) {
        this.status = status;
    }

    protected BaseResponseWrapper(int status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * 使之不在json序列化结果当中
     */
    @JsonIgnore
    public boolean isSuccess() {
        return this.status == HttpStatus.OK.value();
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "BaseResponseWrapper{" +
                "status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}
```
```java
public class ResponseDataWrapper<T> extends BaseResponseWrapper<T> {

    private T data;

    protected ResponseDataWrapper() {
        super(HttpStatus.OK);
    }

    protected ResponseDataWrapper(HttpStatus status) {
        super(status);
    }

    protected ResponseDataWrapper(HttpStatus status, T data) {
        super(status);
        this.data = data;
    }

    protected ResponseDataWrapper(T data) {
        super(HttpStatus.OK);
        this.data = data;
    }

    protected ResponseDataWrapper(int status, String message) {
        super(status, message);
    }

    protected ResponseDataWrapper(int status, T data) {
        super(status);
        this.data = data;
    }

    protected ResponseDataWrapper(int status, String message, T data) {
        super(status, message);
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
```
- 结果封装工程类
```java
public class ResponseDataFactory {

    /**
     * Create by success server response.
     *
     * @param <T> the type parameter
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> success() {
        return new ResponseDataWrapper<T>();
    }

    /**
     * Create by success message server response.
     *
     * @param <T>     the type parameter
     * @param message the message
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> successMessage(String message) {
        return new ResponseDataWrapper<T>(HttpStatus.OK.value(), message);
    }

    /**
     * Create by success server response.
     *
     * @param <T>  the type parameter
     * @param data the data
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> success(T data) {
        return new ResponseDataWrapper<T>(data);
    }

    /**
     * Create by success server response.
     *
     * @param <T>     the type parameter
     * @param message the message
     * @param data    the data
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> success(String message, T data) {
        return new ResponseDataWrapper<T>(HttpStatus.OK.value(), message, data);
    }

    /**
     * Create by error server response.
     *
     * @param <T> the type parameter
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> error() {
        return new ResponseDataWrapper<T>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Create by error message server response.
     *
     * @param <T>          the type parameter
     * @param errorMessage the error message
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> errorMessage(String errorMessage) {
        return new ResponseDataWrapper<T>(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage);
    }

    /**
     * Create by error code message server response.
     *
     * @param <T>          the type parameter
     * @param errorCode    the error code
     * @param errorMessage the error message
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> errorCodeMessage(int errorCode, String errorMessage) {
        return new ResponseDataWrapper<T>(errorCode, errorMessage);
    }

    /**
     * Create by error server response.
     *
     * @param <T>          the type parameter
     * @param errorMessage the error message
     * @param data         the data
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> errorMessageData(String errorMessage, T data) {
        return new ResponseDataWrapper<T>(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage, data);
    }

    /**
     * Create by error server response.
     *
     * @param <T>        the type parameter
     * @param httpStatus the error status
     * @return the server response
     */
    public static <T> ResponseDataWrapper<T> errorHttpStatus(HttpStatus httpStatus) {
        return new ResponseDataWrapper<T>(httpStatus);
    }

    /**
     * 数据+响应码+自定义msg
     *
     * @param data    数据
     * @param code    响应码
     * @param message 自定义msg
     * @author Blare
     */
    public static <T> ResponseDataWrapper<T> wrapper(int code, String message, T data) {
        return new ResponseDataWrapper<>(code, message, data);
    }

    /**
     * 响应状态+数据
     *
     * @param httpStatus 状态
     * @param data       数据
     * @author Blare
     */
    public static <T> ResponseDataWrapper<T> wrapper(HttpStatus httpStatus, T data) {
        return new ResponseDataWrapper<>(httpStatus, data);
    }

}
```
- 响应封装处理器
```java
@RestControllerAdvice("com.itblare.api") // 注意：这里要加上需要扫描的包
public class ResponseDataHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {

        // 如果接口返回的类型本身就是ResultVO那就没有必要进行额外的操作，返回false
        if (methodParameter.getGenericParameterType().equals(ResponseDataWrapper.class)) {
            return false;
        }
        final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        final HttpServletRequest request = requestAttributes.getRequest();
        // 判定请求是否包含标记
        final Boolean hasMark = (Boolean) request.getAttribute(CommonConstant.RESPONSE_WRAPPER_ANN);
        return !Objects.isNull(hasMark) && hasMark;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType mediaType, Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest request, ServerHttpResponse response) {

        // 响应的Content-Type为JSON格式
        if (mediaType.includes(MediaType.APPLICATION_JSON) || MediaType.APPLICATION_JSON.equals(mediaType)) {
            // JSON 返回
            // mediaType 根据实际类型获取的
            if (Objects.isNull(body)) {
                return ResponseDataFactory.success();
            }
            // 已经值指定类型或其他无需包装类型，无需重写
            if (body instanceof ResponseDataWrapper) {
                return body;
            }
            return ResponseDataFactory.success(body);
        }

        //处理返回值是String的情况
        if (Objects.nonNull(body) && body instanceof String) {
            try {
                return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(ResponseDataFactory.success(body));
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
                throw new BaseException(ex);
            }
        }
        return body;
    }

}
```

## 四、多环境配置（Multi-environment configuration）
### 4.1 配置文件查找路径
1. 项目根目录config文件夹里面（优先级最高）：`./config/`
2. 项目根目录：`./`
3. src/main/resources/config/文件夹里面：`classpath:/config`
4. src/main/resources/：`classpath:/`

### 4.2 多开发环境配置
1. 在 src/main/resources目录构建 application.yml，内容如下：
    ```yaml
    # server 配置
    server:
      servlet:
        encoding:
          charset: UTF-8
          enabled: true
      http2:
        enabled: true
    # Spring 配置
    spring:
      profiles:
        active: '@profiles.active@'
    ```
2. 在 src/main/resources/config目录构建 application-xxx.yml，这里xxx分别可以是：dev、test、uat、prod
    ```yaml
    # server 配置
    server:
      # 根据具体环境设置端口
      port: 8080
      servlet:
        # 根据具体环境设置访问根路径
        context-path: /framework-dev
    # Spring 配置
    spring:
      config:
        activate:
          # 根据具体环境设置标识，后面pom文件中需要用到
          on-profile: dev
    ```
3. 本架构基于 maven 构建，所以在pom进行环境编译配置
   ```xml
       <!--环境文件配置-->
    <profiles>
        <!--开发环境-->
        <profile>
            <id>dev</id>
            <properties>
                <profiles.active>dev</profiles.active>
            </properties>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
        </profile>
        <!--测试环境-->
        <profile>
            <id>test</id>
            <properties>
                <profiles.active>test</profiles.active>
            </properties>
        </profile>
        <!--预生产环境-->
        <profile>
            <id>uat</id>
            <properties>
                <profiles.active>uat</profiles.active>
            </properties>
        </profile>
        <!--生产环境-->
        <profile>
            <id>prod</id>
            <properties>
                <profiles.active>prod</profiles.active>
            </properties>
        </profile>
    </profiles>
   ```
   ```xml
    <build>
        <!--资源处理-->
        <resources>
            <!-- java文件 -->
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>*.*</include>
                </includes>
                <filtering>false</filtering>
            </resource>
            <!-- 资源文件 -->
            <resource>
                <directory>src/main/resources</directory>
                <includes>
                    <include>**/application.properties</include>
                    <include>**/application.yml</include>
                    <!--加载模板文件-->
                    <include>**/*.html</include>
                    <!--加载静态文件-->
                    <include>/static/**</include>
                </includes>
                <filtering>true</filtering>
            </resource>
            <!-- 配置文件文件 -->
            <resource>
                <directory>src/main/resources</directory>
                <includes>
                    <include>**/*-${profiles.active}.yml</include>
                    <include>**/*-${profiles.active}.xml</include>
                </includes>
                <filtering>true</filtering>
            </resource>
        </resources>
        <plugins>
            <!-- 解析插件 -->
            <!--<plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <configuration>
                    <delimiters>@</delimiters>
                    <useDefaultDelimiters>false</useDefaultDelimiters>
                </configuration>
            </plugin>-->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
   ```

### 4.3 相关使用命令
- 编译：`mvn clean:clean compile -Dmaven.test.skip=true -P ${profile}`
- 打包：`mvn clean:clean package -Dmaven.test.skip=true -P ${profile}`

## 五、Maven转Gradle（Maven to Gradle project）

- 安装Gradle
- 打开命令行窗口，切换到你的maven工程的pom.xml文件所在目录，然后执行如下命令：
  `gradle init --type pom`
- 最终显示项目目录（成功）

  ![Framework Dir](image\??.png)

## 六、smart-doc集成
smart-doc是一款同时支持JAVA REST API和Apache Dubbo RPC接口文档生成的工具。

### 6.1 添加smart-doc maven 插件
```xml
<plugin>
    <groupId>com.github.shalousun</groupId>
    <artifactId>smart-doc-maven-plugin</artifactId>
    <version>[最新版本]</version>
    <configuration>
        <!--指定生成文档的使用的配置文件,配置文件放在自己的项目中-->
        <configFile>./src/main/resources/smart-doc.json</configFile>
        <!--指定项目名称-->
        <projectName>测试</projectName>
        <!--smart-doc实现自动分析依赖树加载第三方依赖的源码，如果一些框架依赖库加载不到导致报错，这时请使用excludes排除掉-->
        <excludes>
            <!--格式为：groupId:artifactId;参考如下-->
            <exclude>com.alibaba:fastjson</exclude>
        </excludes>
        <!--自1.0.8版本开始，插件提供includes支持,配置了includes后插件会按照用户配置加载而不是自动加载，因此使用时需要注意-->
        <!--smart-doc能自动分析依赖树加载所有依赖源码，原则上会影响文档构建效率，因此你可以使用includes来让插件加载你配置的组件-->
        <includes>
            <!--格式为：groupId:artifactId;参考如下-->
            <include>com.alibaba:fastjson</include>
        </includes>
    </configuration>
    <executions>
        <execution>
            <!--如果不需要在执行编译时启动smart-doc，则将phase注释掉-->
            <phase>compile</phase>
            <goals>
                <!--smart-doc提供了html、openapi、markdown等goal，可按需配置-->
                <goal>html</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 6.2 详细配置说明
```text
{
  "serverUrl": "http://127.0.0.1", //服务器地址,非必须。导出postman建议设置成protocol://{{server}}方便直接在postman直接设置环境变量
  "isStrict": false, //是否开启严格模式
  "allInOne": true,  //是否将文档合并到一个文件中，一般推荐为true
  "outPath": "D://md2", //指定文档的输出路径
  "coverOld": true,  //是否覆盖旧的文件，主要用于mardown文件覆盖
  "createDebugPage": true,//@since 2.0.0 smart-doc支持创建可以测试的html页面，仅在AllInOne模式中起作用。
  "packageFilters": "",//controller包过滤，多个包用英文逗号隔开
  "md5EncryptedHtmlName": false,//只有每个controller生成一个html文件是才使用
  "style":"xt256", //基于highlight.js的代码高设置,可选值很多可查看码云wiki，喜欢配色统一简洁的同学可以不设置
  "projectName": "smart-doc",//配置自己的项目名称
  "skipTransientField": true,//目前未实现
  "sortByTitle":false,//接口标题排序，默认为false,@since 1.8.7版本开始
  "showAuthor":true,//是否显示接口作者名称，默认是true,不想显示可关闭
  "requestFieldToUnderline":true,//自动将驼峰入参字段在文档中转为下划线格式,//@since 1.8.7版本开始
  "responseFieldToUnderline":true,//自动将驼峰入参字段在文档中转为下划线格式,//@since 1.8.7版本开始
  "inlineEnum":true,//设置为true会将枚举详情展示到参数表中，默认关闭，//@since 1.8.8版本开始
  "recursionLimit":7,//设置允许递归执行的次数用于避免一些对象解析卡主，默认是7，正常为3次以内，//@since 1.8.8版本开始
  "allInOneDocFileName":"index.html",//自定义设置输出文档名称, @since 1.9.0
  "requestExample":"true",//是否将请求示例展示在文档中，默认true，@since 1.9.0
  "responseExample":"true",//是否将响应示例展示在文档中，默认为true，@since 1.9.0
  "urlSuffix":".do",//支持SpringMVC旧项目的url后缀,@since 2.1.0
  "displayActualType":false,//配置true会在注释栏自动显示泛型的真实类型短类名，@since 1.9.6
  "appKey": "20201216788835306945118208",// torna平台对接appKey,, @since 2.0.9
  "appToken": "c16931fa6590483fb7a4e85340fcbfef", //torna平台appToken,@since 2.0.9
  "secret": "W.ZyGMOB9Q0UqujVxnfi@.I#V&tUUYZR",//torna平台secret，@since 2.0.9
  "openUrl": "http://localhost:7700/api",//torna平台地址，填写自己的私有化部署地址@since 2.0.9
  "debugEnvName":"测试环境", //torna环境名称
  "debugEnvUrl":"http://127.0.0.1",//推送torna配置接口服务地址
  "tornaDebug":false,//启用会推送日志
  "ignoreRequestParams":[ //忽略请求参数对象，把不想生成文档的参数对象屏蔽掉，@since 1.9.2
     "org.springframework.ui.ModelMap"
   ],
  "dataDictionaries": [{ //配置数据字典，没有需求可以不设置
      "title": "http状态码字典", //数据字典的名称
      "enumClassName": "com.power.common.enums.HttpCodeEnum", //数据字典枚举类名称
      "codeField": "code",//数据字典字典码对应的字段名称
      "descField": "message"//数据字典对象的描述信息字典
  }],
  "errorCodeDictionaries": [{ //错误码列表，没有需求可以不设置
    "title": "title",
    "enumClassName": "com.power.common.enums.HttpCodeEnum", //错误码枚举类
    "codeField": "code",//错误码的code码字段名称
    "descField": "message"//错误码的描述信息对应的字段名
  }],
  "revisionLogs": [{ //文档变更记录，非必须
      "version": "1.0", //文档版本号
      "revisionTime": "2020-12-31 10:30", //文档修订时间
      "status": "update", //变更操作状态，一般为：创建、更新等
      "author": "author", //文档变更作者
      "remarks": "desc" //变更描述
    }
  ],
  "customResponseFields": [{ //自定义添加字段和注释，api-doc后期遇到同名字段则直接给相应字段加注释，非必须
      "name": "code",//覆盖响应码字段
      "desc": "响应代码",//覆盖响应码的字段注释
      "ownerClassName": "org.springframework.data.domain.Pageable", //指定你要添加注释的类名
      "value": "00000"//设置响应码的值
  }],
  "customRequestFields": [{ //自定义请求体的注释，@since 2.1.3，非必须
       "name":"code", //属性名
       "desc":"状态码", //描述
       "ownerClassName":"com.xxx.constant.entity.Result", //属性对应的类全路径
       "value":"200", //默认值或者mock值
       "required":true, //是否必填
       "ignore":false //是否忽略
  }],
  "requestHeaders": [{ //设置请求头，没有需求可以不设置
      "name": "token",//请求头名称
      "type": "string",//请求头类型
      "desc": "desc",//请求头描述信息
      "value":"token请求头的值",//不设置默认null
      "required": false,//是否必须
      "since": "-",//什么版本添加的改请求头
      "pathPatterns": "/app/test/**",//请看https://gitee.com/smart-doc-team/smart-doc/wikis/请求头高级配置?sort_id=4178978
      "excludePathPatterns":"/app/page/**"//请看https://gitee.com/smart-doc-team/smart-doc/wikis/请求头高级配置?sort_id=4178978
  },{
      "name": "appkey",//请求头
      "type": "string",//请求头类型
      "desc": "desc",//请求头描述信息
      "value":"appkey请求头的值",//不设置默认null
      "required": false,//是否必须
      "pathPatterns": "/test/add,/testConstants/1.0",//正则表达式过滤请求头,url匹配上才会添加该请求头，多个正则用分号隔开
      "since": "-"//什么版本添加的改请求头
  }],
  "rpcApiDependencies":[{ // 项目开放的dubbo api接口模块依赖，配置后输出到文档方便使用者集成
        "artifactId":"SpringBoot2-Dubbo-Api",
        "groupId":"com.demo",
        "version":"1.0.0"
   }],
  "rpcConsumerConfig": "src/main/resources/consumer-example.conf",//文档中添加dubbo consumer集成配置，用于方便集成方可以快速集成
  "apiObjectReplacements": [{ // 自smart-doc 1.8.5开始你可以使用自定义类覆盖其他类做文档渲染，非必须
      "className": "org.springframework.data.domain.Pageable",
      "replacementClassName": "com.power.doc.model.PageRequestDto" //自定义的PageRequestDto替换Pageable做文档渲染
  }],
  "apiConstants": [{//从1.8.9开始配置自己的常量类，smart-doc在解析到常量时自动替换为具体的值，非必须
        "constantsClassName": "com.power.doc.constants.RequestParamConstant"
  }],
  "responseBodyAdvice":{ //自smart-doc 1.9.8起，非必须项，ResponseBodyAdvice统一返回设置(不要随便配置根据项目的技术来配置)，可用ignoreResponseBodyAdvice tag来忽略
  		"className":"com.power.common.model.CommonResult" //通用响应体
  },
  "requestBodyAdvice":{ ////自smart-doc 2.1.4 起，支持设置RequestBodyAdvice统一请求包装类，非必须
         "className":"com.power.common.model.CommonResult"
  }
}
```

### 6.3 maven 命令生产文档
```text
//生成html
mvn -Dfile.encoding=UTF-8 smart-doc:html
//生成markdown
mvn -Dfile.encoding=UTF-8 smart-doc:markdown
//生成adoc
mvn -Dfile.encoding=UTF-8 smart-doc:adoc
//生成postman json数据
mvn -Dfile.encoding=UTF-8 smart-doc:postman
// 生成 Open Api 3.0+,Since smart-doc-maven-plugin 1.1.5
mvn -Dfile.encoding=UTF-8 smart-doc:openapi
// 生成文档推送到Torna平台
mvn -Dfile.encoding=UTF-8 smart-doc:torna-rest

// Apache Dubbo RPC文档
// Generate html
mvn -Dfile.encoding=UTF-8 smart-doc:rpc-html
// Generate markdown
mvn -Dfile.encoding=UTF-8 smart-doc:rpc-markdown
// Generate adoc
mvn -Dfile.encoding=UTF-8 smart-doc:rpc-adoc

// 生成dubbo接口文档推送到torna
mvn -Dfile.encoding=UTF-8 smart-doc:torna-rpc
```

- IDEA中maven插件
  ![smart-doc maven](image\IDEA smart-doc.png)
- 注意：在window系统下，如果实际使用Maven命令行执行文档生成，可能会出现乱码，因此需要在执行时指定-Dfile.encoding=UTF-8


### 6.4 smart-doc+Torna文档自动化
[Torna官网](http://torna.cn/)

[Torna 使用步骤](https://gitee.com/durcframework/torna#%E4%BD%BF%E7%94%A8%E6%AD%A5%E9%AA%A4)

[smart-doc与torna对接](https://gitee.com/smart-doc-team/smart-doc/wikis/smart-doc%E4%B8%8Etorna%E5%AF%B9%E6%8E%A5?sort_id=3695028)
