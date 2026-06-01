# 用户注册和登录集成测试项目

## 项目概述

这是一个完整的用户注册和登录功能集成测试项目，基于 Spring Boot 3.2.0 + JUnit 5 + MockMvc 构建。

**项目特点：**
- ✅ 真实可运行的 Spring Boot 应用
- ✅ 完整的用户注册和登录业务逻辑
- ✅ 15 个集成测试用例（注册 9 个 + 登录 6 个）
- ✅ 使用 H2 内存数据库进行测试
- ✅ 密码加密存储（BCrypt）
- ✅ 参数校验和异常处理

---

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.2.0
- **测试框架**: JUnit 5
- **Mock 框架**: MockMvc
- **数据库**: H2 (测试) / MySQL (生产)
- **ORM**: Spring Data JPA
- **安全**: Spring Security
- **构建工具**: Maven

---

## 项目结构

```
Software test/
├── pom.xml                                    # Maven 配置文件
├── README.md                                  # 项目说明文档
├── 测试用例文档.md                            # 测试用例详细说明
├── 测试执行指南.md                            # 如何运行测试
└── src/
    ├── main/
    │   ├── java/com/softwaretest/
    │   │   ├── UserAuthApplication.java       # 主应用类
    │   │   ├── entity/
    │   │   │   └── User.java                  # 用户实体
    │   │   ├── repository/
    │   │   │   └── UserRepository.java        # 用户数据访问层
    │   │   ├── dto/
    │   │   │   ├── RegisterRequest.java       # 注册请求 DTO
    │   │   │   ├── LoginRequest.java          # 登录请求 DTO
    │   │   │   └── ApiResponse.java           # 统一响应格式
    │   │   ├── service/
    │   │   │   └── UserService.java           # 用户业务逻辑
    │   │   ├── controller/
    │   │   │   └── AuthController.java        # 认证控制器
    │   │   └── config/
    │   │       └── SecurityConfig.java        # 安全配置
    │   └── resources/
    │       ├── application.yml                # 生产环境配置
    │       └── application-test.yml           # 测试环境配置
    └── test/
        └── java/com/softwaretest/
            ├── UserRegistrationIntegrationTest.java  # 注册测试（9个用例）
            └── UserLoginIntegrationTest.java         # 登录测试（6个用例）
```

---

## 测试用例概览

### 用户注册测试（9个用例）

| 编号 | 测试用例 | 预期结果 |
|------|---------|---------|
| TC-REG-01 | 手机号已存在 | 注册失败，返回 400 |
| TC-REG-02 | 邮箱已存在 | 注册失败，返回 400 |
| TC-REG-03 | 用户名已存在 | 注册失败，返回 400 |
| TC-REG-04 | 手机号长度≠11位 | 注册失败，返回 400 |
| TC-REG-05 | 用户名长度<6位 | 注册失败，返回 400 |
| TC-REG-06 | 用户名长度>24位 | 注册失败，返回 400 |
| TC-REG-07 | 密码长度<6位 | 注册失败，返回 400 |
| TC-REG-08 | 密码长度>24位 | 注册失败，返回 400 |
| TC-REG-09 | 所有字段合法 | 注册成功，返回 200 |

### 用户登录测试（6个用例）

| 编号 | 测试用例 | 预期结果 |
|------|---------|---------|
| TC-LOGIN-01 | 正确的用户名和密码 | 登录成功，返回 200 |
| TC-LOGIN-02 | 用户名不存在 | 登录失败，返回 401 |
| TC-LOGIN-03 | 密码错误 | 登录失败，返回 401 |
| TC-LOGIN-04 | 用户名为空 | 登录失败，返回 401 |
| TC-LOGIN-05 | 密码为空 | 登录失败，返回 401 |
| TC-LOGIN-06 | 注册后立即登录 | 登录成功，返回 200 |

---

## 快速开始

### 1. 环境要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本

### 2. 运行测试

```bash
# 进入项目目录
cd "C:\Users\34445\Desktop\Software test"

# 运行所有测试
mvn test

# 只运行注册测试
mvn test -Dtest=UserRegistrationIntegrationTest

# 只运行登录测试
mvn test -Dtest=UserLoginIntegrationTest

# 运行单个测试方法
mvn test -Dtest=UserRegistrationIntegrationTest#testSuccessfulRegistration
```

### 3. 查看测试报告

测试报告位于：`target/surefire-reports/`

---

## API 接口说明

### 注册接口

**请求：**
```http
POST /api/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "Pass@123",
  "email": "test@example.com",
  "phone": "13800138000",
  "userType": "user"
}
```

**成功响应：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "testuser"
  }
}
```

**失败响应：**
```json
{
  "code": 400,
  "message": "用户名已存在",
  "data": null
}
```

### 登录接口

**请求：**
```http
POST /api/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "Pass@123"
}
```

**成功响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "userType": "user"
  }
}
```

**失败响应：**
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

---

## 数据校验规则

### 用户名
- 不能为空
- 长度：6-24 位
- 唯一性：不能重复

### 密码
- 不能为空
- 长度：6-24 位
- 存储：BCrypt 加密

### 邮箱
- 不能为空
- 格式：符合邮箱格式
- 唯一性：不能重复

### 手机号
- 不能为空
- 格式：11 位数字，以 1 开头
- 唯一性：不能重复

---

## 测试覆盖范围

### 功能测试
- ✅ 用户注册成功流程
- ✅ 用户登录成功流程
- ✅ 重复数据校验（用户名、邮箱、手机号）
- ✅ 字段长度校验
- ✅ 字段格式校验
- ✅ 密码加密验证

### 异常测试
- ✅ 重复注册处理
- ✅ 错误密码处理
- ✅ 用户不存在处理
- ✅ 空字段处理
- ✅ 非法格式处理

---

## 启动应用（可选）

如果需要启动应用进行手动测试：

```bash
# 确保 MySQL 已启动并创建数据库
mysql -u root -p
CREATE DATABASE user_auth_test;

# 启动应用
mvn spring-boot:run

# 应用将运行在 http://localhost:8080
```

---

## 常见问题

### Q: 测试失败怎么办？

A: 检查以下几点：
1. JDK 版本是否为 17 或更高
2. Maven 依赖是否下载完整
3. 查看 `target/surefire-reports/` 中的详细错误信息

### Q: 如何修改数据库配置？

A: 编辑 `src/main/resources/application.yml` 文件

### Q: 如何添加新的测试用例？

A: 参考现有测试类，在对应的测试类中添加新的 `@Test` 方法

---

## 文档说明

- **README.md**（本文件）：项目总体说明
- **测试用例文档.md**：详细的测试用例说明
- **测试执行指南.md**：测试执行步骤和注意事项

---

## 作者

软件测试实践项目

**创建日期：** 2026年6月1日
