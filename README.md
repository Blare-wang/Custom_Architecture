# 基于Spring boot 快速构建骨架
## 一、简介
### 1.1 背景

## 二、多环境配置（Multi-environment configuration）
### 2.1 配置文件查找路径
1. 项目根目录config文件夹里面（优先级最高）：`./config/`
2. 项目根目录：`./`
3. src/main/resources/config/文件夹里面：`classpath:/config`
4. src/main/resources/：`classpath:/`
### 2.2 多开发环境配置
1. 在 src/main/resources目录构建 application.yml，内容如下：
    ```
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
    ```
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
    ```
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
### 2.3 相关使用命令
编译：`mvn clean:clean compile -Dmaven.test.skip=true -P ${profile}`

打包：`mvn clean:clean package -Dmaven.test.skip=true -P ${profile}`

## 三、Maven转Gradle（Maven to Gradle project）
- 安装Gradle
- 打开命令行窗口，切换到你的maven工程的pom.xml文件所在目录，然后执行如下命令：
`gradle init --type pom`
- 最终显示项目目录（成功）

    ![Framework Dir](image\??.png)
    
## 三、接口HTTP响应统一处理（HTTP response configuration）
HTTP请求指令返回状态

- 201~206 都表示服务器成功处理了请求的状态代码，说明网页可以正常访问。
    - 200（成功） 服务器已成功处理了请求。通常，这表示服务器提供了请求的网页。
    - 201（已创建） 请求成功且服务器已创建了新的资源。
    - 202（已接受） 服务器已接受了请求，但尚未对其进行处理。
    - 203（非授权信息） 服务器已成功处理了请求，但返回了可能来自另一来源的信息。
    - 204（无内容） 服务器成功处理了请求，但未返回任何内容。
    - 205（重置内容） 服务器成功处理了请求，但未返回任何内容。与 204 响应不同，此响应要求请求者重置文档视图（例如清除表单内容以输入新内容）。
    - 206（部分内容） 服务器成功处理了部分 GET 请求。
- 300-~3007 表示：要完成请求，需要进一步进行操作。通常，这些状态代码是永远重定向的。
    - 300（多种选择） 服务器根据请求可执行多种操作。服务器可根据请求者 来选择一项操作，或提供操作列表供其选择。
    - 301（永久移动） 请求的网页已被永久移动到新位置。服务器返回此响应时，会自动将请求者转到新位置。您应使用此代码通知搜索引擎蜘蛛网页或网站已被永久移动到新位置。
    - 302（临时移动） 服务器目前正从不同位置的网页响应请求，但请求者应继续使用原有位置来进行以后的请求。会自动将请求者转到不同的位置。但由于搜索引擎会继续抓取原有位置并将其编入索引，因此您不应使用此代码来告诉搜索引擎页面或网站已被移动。
    - 303（查看其他位置） 当请求者应对不同的位置进行单独的 GET 请求以检索响应时，服务器会返回此代码。对于除 HEAD 请求之外的所有请求，服务器会自动转到其他位置。
    - 304（未修改） 自从上次请求后，请求的网页未被修改过。服务器返回此响应时，不会返回网页内容。
    如果网页自请求者上次请求后再也没有更改过，您应当将服务器配置为返回此响应。由于服务器可以告诉 搜索引擎自从上次抓取后网页没有更改过，因此可节省带宽和开销。
    - 305（使用代理） 请求者只能使用代理访问请求的网页。如果服务器返回此响应，那么，服务器还会指明请求者应当使用的代理。
    - 307（临时重定向） 服务器目前正从不同位置的网页响应请求，但请求者应继续使用原有位置来进行以后的请求。会自动将请求者转到不同的位置。但由于搜索引擎会继续抓取原有位置并将其编入索引，因此您不应使用此代码来告诉搜索引擎某个页面或网站已被移动。
- 4XX 表示请求可能出错，会妨碍服务器的处理。
    - 400（错误请求） 服务器不理解请求的语法。
    - 401（身份验证错误） 此页要求授权。您可能不希望将此网页纳入索引。
    - 403（禁止） 服务器拒绝请求。
    - 404（未找到） 服务器找不到请求的网页。例如，对于服务器上不存在的网页经常会返回此代码。
    - 405（方法禁用） 禁用请求中指定的方法。
    - 406（不接受） 无法使用请求的内容特性响应请求的网页。
    - 407（需要代理授权） 此状态码与 401 类似，但指定请求者必须授权使用代理。如果服务器返回此响应，还表示请求者应当使用代理。
    - 408（请求超时） 服务器等候请求时发生超时。
    - 409（冲突） 服务器在完成请求时发生冲突。服务器必须在响应中包含有关冲突的信息。服务器在响应与前一个请求相冲突的 PUT 请求时可能会返回此代码，以及两个请求的差异列表。
    - 410（已删除） 请求的资源永久删除后，服务器返回此响应。该代码与 404（未找到）代码相似，但在资源以前存在而现在不存在的情况下，有时会用来替代 404 代码。如果资源已永久删除，您应当使用 301 指定资源的新位置。
    - 411（需要有效长度） 服务器不接受不含有效内容长度标头字段的请求。
    - 412（未满足前提条件） 服务器未满足请求者在请求中设置的其中一个前提条件。
    - 413（请求实体过大） 服务器无法处理请求，因为请求实体过大，超出服务器的处理能力。
    - 414（请求的 URI 过长） 请求的 URI（通常为网址）过长，服务器无法处理。
    - 415（不支持的媒体类型） 请求的格式不受请求页面的支持。
    - 416（请求范围不符合要求） 如果页面无法提供请求的范围，则服务器会返回此状态码。
    - 417（未满足期望值） 服务器未满足"期望"请求标头字段的要求。
- 500~505 表示的意思是：服务器在尝试处理请求时发生内部错误。这些错误可能是服务器本身的错误，而不是请求出错。
    - 500（服务器内部错误） 服务器遇到错误，无法完成请求。
    - 501（尚未实施） 服务器不具备完成请求的功能。例如，当服务器无法识别请求方法时，服务器可能会返回此代码。
    - 502（错误网关） 服务器作为网关或代理，从上游服务器收到了无效的响应。
    - 503（服务不可用） 目前无法使用服务器（由于超载或进行停机维护）。通常，这只是一种暂时的状态。
    - 504（网关超时） 服务器作为网关或代理，未及时从上游服务器接收请求。
    - 505（HTTP 版本不受支持） 服务器不支持请求中所使用的 HTTP 协议版本。
