# MyFitPet Spring Boot Server

## 快速开始

### 先决条件
- Java 17
- Maven 3.9+
- 本地 MySQL，创建数据库 `myfitpet`

### 配置
修改 `src/main/resources/application.yml` 中的数据源：
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.datasource.url` 如需变更端口或库名

### 运行
```bat
cd backend\springboot-server
mvn clean package
mvn spring-boot:run
```

### 健康检查
- GET `http://localhost:8080/api/health`

### 示例接口
- GET `http://localhost:8080/api/users`
- POST `http://localhost:8080/api/users`
- GET `http://localhost:8080/api/users/{id}`
- POST `http://localhost:8080/api/auth/login` (占位)

### 后续计划
- 接入真实 JWT 登录与鉴权
- 完善用户模型与领域逻辑
- 添加统一异常处理与日志
