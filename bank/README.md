# 金融分布式交易系统（后端）

基于 Spring Boot + MyBatis，连接 MyCat 的最小可运行示例：开户、登录、查询、转账、计息、对账。

## 运行环境
- JDK 17
- Maven 3.9+
- MyCat（示例端口 8066）与 MySQL 8.x

## 快速启动
1. 修改 `src/main/resources/application.yml` 数据源配置。
2. 执行表结构脚本：`docs/sql/schema.sql`。
3. 启动服务：
```bash
mvn spring-boot:run
```

## 接口概览
- 认证：`POST /api/auth/register`，`POST /api/auth/login`
- 账户：`POST /api/account/open`，`GET /api/account/{accountNo}`，`POST /api/account/transfer`，`GET /api/account/tx/{accountNo}`
- 运维：`POST /api/ops/accrue-daily`，`POST /api/ops/settle-monthly`
- 对账：`POST /api/reconcile`

## MyCat 提示
- 逻辑库：`bankdb`，示例连接：`jdbc:mysql://127.0.0.1:8066/bankdb`
- 分片建议：`t_user` 按用户/用户名，`t_account` 按账户号，`t_tx` 按账户或时间分片。

## 说明
- 计息/对账为占位实现，生产需补充批处理SQL、幂等重试、日志审计与分布式事务。
- 已配置CORS支持前端跨域访问（localhost:8081）

