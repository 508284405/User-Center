# User Center

多模块 Maven 项目（Java 17, Spring Boot 3），采用 DDD + COLA 分层架构。

## 模块说明
- user-center-client：公共 API 接口与 DTO/Command/Query 等契约。
- user-center-adapter：Web 层适配（Controller、全局异常处理、Web 配置）。
- user-center-app：应用服务与编排（ServiceImpl、CmdExe/QryExe、调度等）。
- user-center-domain：领域模型与领域服务（实体、值对象、DomainService、Gateway 接口）。
- user-center-infrastructure：基础设施实现（MyBatis(Plus) Mapper、DO、外部集成、配置）。
- start：Spring Boot 启动模块与运行时配置（application.yml、keystore 等）。

## 目录说明
- 源码仅位于上述 user-center-* 模块与 start 模块内。
- 为避免混淆，根目录下的非模块化空目录 adapter/、app/、client/、domain/、infrastructure 已移除。
- 各模块资源位于各自 `src/main/resources`，如 MyBatis XML 位于 `user-center-infrastructure/src/main/resources`。

## 构建/测试/运行（本地）
- 构建全部：`mvn clean install -DskipTests`
- 运行测试：`mvn test`
- 运行单模块测试：`mvn -pl user-center-domain -am test`
- 启动（dev）：`mvn -pl start spring-boot:run`
- 运行 jar：`java -jar start/target/start-1.0.0-SNAPSHOT.jar`

## 其他
- 配置与敏感信息：通过环境变量或配置中心注入（例如 `SPRING_PROFILES_ACTIVE=dev`）。
- JWT 密钥位于 `start/src/main/resources/keystore`，勿提交明文密钥。
