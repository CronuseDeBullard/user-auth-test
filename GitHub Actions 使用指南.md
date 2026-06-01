# GitHub Actions 集成测试使用指南

## 📋 目录

1. [什么是 GitHub Actions](#什么是-github-actions)
2. [工作流配置说明](#工作流配置说明)
3. [如何使用](#如何使用)
4. [查看测试结果](#查看测试结果)
5. [常见问题](#常见问题)

---

## 什么是 GitHub Actions

GitHub Actions 是 GitHub 提供的持续集成/持续部署（CI/CD）服务，可以自动化执行测试、构建和部署等任务。

**优势：**
- ✅ 代码提交后自动运行测试
- ✅ 并行执行多个测试任务
- ✅ 自动生成测试报告
- ✅ 免费使用（公开仓库）

---

## 工作流配置说明

### 配置文件位置

```
.github/workflows/integration-test.yml
```

### 工作流结构

我们的工作流包含 **4 个任务（Jobs）**：

#### 1. test-user-registration（用户注册功能测试）
- 运行 9 个注册测试用例
- 上传测试报告
- 生成测试摘要

#### 2. test-user-login（用户登录功能测试）
- 运行 6 个登录测试用例
- 上传测试报告
- 生成测试摘要

#### 3. test-all（完整测试套件）
- 运行所有 15 个测试用例
- 依赖前两个任务完成
- 生成完整测试统计

#### 4. test-summary（测试结果汇总）
- 汇总所有测试结果
- 下载所有测试报告
- 生成最终汇总报告

### 触发条件

工作流会在以下情况自动运行：

```yaml
on:
  push:
    branches: [ main, master, develop ]  # 推送到这些分支时
  pull_request:
    branches: [ main, master ]           # 创建 PR 到这些分支时
  workflow_dispatch:                     # 手动触发
```

---

## 如何使用

### 步骤 1：创建 GitHub 仓库

```bash
# 在项目目录下初始化 Git
cd "C:\Users\34445\Desktop\Software test"
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit: 用户注册和登录集成测试项目"

# 关联远程仓库（替换为你的仓库地址）
git remote add origin https://github.com/your-username/user-auth-test.git

# 推送到 GitHub
git branch -M main
git push -u origin main
```

### 步骤 2：工作流自动运行

推送代码后，GitHub Actions 会自动运行测试。

### 步骤 3：手动触发测试

如果需要手动运行测试：

1. 进入 GitHub 仓库页面
2. 点击 **Actions** 标签
3. 选择 **用户认证集成测试** 工作流
4. 点击 **Run workflow** 按钮
5. 选择分支，点击 **Run workflow**

---

## 查看测试结果

### 方式 1：在 Actions 页面查看

1. 进入仓库的 **Actions** 标签
2. 点击最近的工作流运行记录
3. 查看各个任务的执行状态

**示例界面：**
```
✅ test-user-registration  (用户注册功能测试)
✅ test-user-login         (用户登录功能测试)
✅ test-all                (完整测试套件)
✅ test-summary            (测试结果汇总)
```

### 方式 2：查看测试摘要

每个任务都会生成测试摘要，显示在工作流运行页面的 **Summary** 部分。

**摘要内容包括：**
- 总测试数
- 通过数
- 失败数
- 错误数
- 测试状态

### 方式 3：下载测试报告

测试报告会作为 **Artifacts** 上传，可以下载查看详细信息。

**下载步骤：**
1. 进入工作流运行页面
2. 滚动到页面底部的 **Artifacts** 部分
3. 下载对应的测试报告：
   - `registration-test-report`（注册测试报告）
   - `login-test-report`（登录测试报告）
   - `all-test-reports`（完整测试报告）

---

## 工作流执行流程图

```
代码推送/PR/手动触发
         ↓
    触发工作流
         ↓
    ┌────┴────┐
    ↓         ↓
注册测试   登录测试  (并行执行)
    ↓         ↓
    └────┬────┘
         ↓
    完整测试套件
         ↓
    测试结果汇总
         ↓
    生成报告和摘要
```

---

## 测试报告示例

### 测试摘要示例

```markdown
## 用户注册测试结果 📝

- 总测试数: 9
- 失败数: 0
- 错误数: 0
- 状态: ✅ 全部通过
```

### 完整测试报告示例

```markdown
# 📊 完整测试报告

## 测试统计

- 📝 总测试用例数: 15
- ✅ 通过: 15
- ❌ 失败: 0
- ⚠️ 错误: 0

## ✅ 测试状态: 全部通过

## 测试模块详情

| 测试模块 | 用例数 | 状态 |
|---------|--------|------|
| 用户注册功能 | 9 | ✅ |
| 用户登录功能 | 6 | ✅ |
```

---

## 常见问题

### Q1: 为什么测试失败了？

**可能原因：**
1. 代码有 bug
2. 测试用例编写错误
3. 依赖下载失败

**解决方法：**
1. 查看失败任务的日志
2. 下载测试报告查看详细错误信息
3. 在本地运行 `mvn test` 复现问题

### Q2: 如何只运行特定的测试？

修改 `.github/workflows/integration-test.yml` 中的测试命令：

```yaml
# 只运行注册测试
- name: 运行用户注册测试
  run: mvn test -Dtest=UserRegistrationIntegrationTest

# 只运行登录测试
- name: 运行用户登录测试
  run: mvn test -Dtest=UserLoginIntegrationTest

# 运行所有测试
- name: 运行所有测试
  run: mvn test
```

### Q3: 如何修改触发条件？

编辑 `.github/workflows/integration-test.yml` 的 `on` 部分：

```yaml
# 只在推送到 main 分支时运行
on:
  push:
    branches: [ main ]

# 每天定时运行（UTC 时间 0:00）
on:
  schedule:
    - cron: '0 0 * * *'

# 只手动触发
on:
  workflow_dispatch:
```

### Q4: 测试报告保存多久？

默认保存 **30 天**，可以修改 `retention-days` 参数：

```yaml
- name: 上传测试报告
  uses: actions/upload-artifact@v4
  with:
    name: test-report
    path: target/surefire-reports/
    retention-days: 7  # 修改为 7 天
```

### Q5: 如何添加 MySQL 数据库？

如果测试需要真实的 MySQL 数据库，可以添加服务容器：

```yaml
jobs:
  test-with-mysql:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: user_auth_test
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3
    
    steps:
      - name: 运行测试
        run: mvn test
        env:
          SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/user_auth_test
          SPRING_DATASOURCE_USERNAME: root
          SPRING_DATASOURCE_PASSWORD: root
```

### Q6: 如何查看详细的测试日志？

1. 进入 Actions 页面
2. 点击工作流运行记录
3. 点击具体的任务（如 "用户注册功能测试"）
4. 展开 "运行用户注册测试" 步骤
5. 查看完整的 Maven 测试输出

---

## 高级配置

### 并行执行策略

当前配置使用 **并行执行** 策略：

```yaml
# 注册测试和登录测试并行执行
test-user-registration:
  runs-on: ubuntu-latest
  # 无依赖，立即执行

test-user-login:
  runs-on: ubuntu-latest
  # 无依赖，立即执行

# 完整测试等待前两个完成
test-all:
  needs: [test-user-registration, test-user-login]
```

### 添加测试覆盖率报告

可以集成 JaCoCo 生成测试覆盖率报告：

```yaml
- name: 生成测试覆盖率报告
  run: mvn jacoco:report

- name: 上传覆盖率报告
  uses: codecov/codecov-action@v3
  with:
    files: target/site/jacoco/jacoco.xml
```

---

## 最佳实践

1. **保持测试快速**：集成测试应该在 5 分钟内完成
2. **使用内存数据库**：测试环境使用 H2，避免依赖外部数据库
3. **并行执行**：独立的测试模块并行运行，提高效率
4. **保留测试报告**：设置合理的保留天数，便于问题追踪
5. **定期运行**：除了代码推送，可以设置定时任务定期运行测试

---

## 相关资源

- [GitHub Actions 官方文档](https://docs.github.com/en/actions)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)

---

**文档版本：** 1.0  
**创建日期：** 2026年6月1日  
**最后更新：** 2026年6月1日
