# GitHub Actions 集成测试指南

## 📋 概述

本指南说明如何在GitHub Actions上运行新创建的集成测试。

---

## 🚀 快速开始

### 步骤1: 提交代码到GitHub

```bash
# 进入项目目录
cd "C:\Users\34445\Desktop\Software test"

# 添加所有文件
git add .

# 提交
git commit -m "添加集成测试"

# 推送到GitHub
git push origin main
```

### 步骤2: 查看测试运行

1. 打开GitHub仓库：https://github.com/CronuseDeBullard/user-auth-test
2. 点击顶部的 **Actions** 标签
3. 您会看到 "新集成测试" workflow 正在运行

---

## 📁 Workflow文件说明

### 新创建的Workflow

**文件位置：** `.github/workflows/new-integration-tests.yml`

**包含的测试任务：**

| 任务名称 | 测试类 | 用例数 | 说明 |
|---------|--------|--------|------|
| test-user-authentication | UserAuthenticationIntegrationTest | 6 | 用户认证集成测试 |
| test-teacher-exam | TeacherExamIntegrationTest | 8 | 教师端考试管理集成测试 |
| test-student-exam | StudentExamIntegrationTest | 7 | 学生端考试集成测试 |
| test-all-integration | *IntegrationTest | 21 | 所有集成测试 |
| test-summary | - | - | 测试结果汇总 |

---

## 🎯 触发方式

### 1. 自动触发

Workflow会在以下情况自动运行：

```yaml
on:
  push:
    branches: [ main, master, develop ]  # 推送到这些分支时
  pull_request:
    branches: [ main, master ]           # 创建PR时
```

### 2. 手动触发

1. 进入GitHub仓库
2. 点击 **Actions** 标签
3. 左侧选择 "新集成测试"
4. 点击右侧 **Run workflow** 按钮
5. 选择分支
6. 点击绿色的 **Run workflow** 按钮

![手动触发示例](https://docs.github.com/assets/cb-33882/images/help/actions/workflow-dispatch-button.png)

---

## 📊 查看测试结果

### 方法1: 在Actions页面查看

1. 进入 **Actions** 标签
2. 点击最新的workflow运行
3. 查看各个任务的状态：
   - ✅ 绿色勾号 = 测试通过
   - ❌ 红色叉号 = 测试失败
   - 🟡 黄色圆圈 = 正在运行

### 方法2: 查看测试摘要

每个任务都会生成测试摘要，显示：
- 📝 总测试数
- ✅ 通过数
- ❌ 失败数
- ⚠️ 错误数
- ⏭️ 跳过数

### 方法3: 下载测试报告

1. 进入workflow运行页面
2. 滚动到底部的 **Artifacts** 部分
3. 下载测试报告：
   - `user-auth-test-report` - 用户认证测试报告
   - `teacher-exam-test-report` - 教师端测试报告
   - `student-exam-test-report` - 学生端测试报告
   - `all-integration-test-reports` - 完整测试报告

---

## 🔧 Workflow配置详解

### Job 1: test-user-authentication

```yaml
test-user-authentication:
  name: 用户认证集成测试
  runs-on: ubuntu-latest  # 运行环境
  
  steps:
    - name: 检出代码
      uses: actions/checkout@v4
    
    - name: 设置 JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven  # 缓存Maven依赖
    
    - name: 运行用户认证集成测试
      run: mvn test -Dtest=UserAuthenticationIntegrationTest
    
    - name: 上传测试报告
      if: always()  # 即使测试失败也上传
      uses: actions/upload-artifact@v4
      with:
        name: user-auth-test-report
        path: target/surefire-reports/
        retention-days: 30  # 保留30天
```

### Job 2: test-teacher-exam

类似的配置，运行 `TeacherExamIntegrationTest`

### Job 3: test-student-exam

类似的配置，运行 `StudentExamIntegrationTest`

### Job 4: test-all-integration

```yaml
test-all-integration:
  name: 所有集成测试
  runs-on: ubuntu-latest
  needs: [test-user-authentication, test-teacher-exam, test-student-exam]  # 依赖前面的任务
  
  steps:
    - name: 运行所有集成测试
      run: mvn test -Dtest=*IntegrationTest  # 运行所有集成测试
```

### Job 5: test-summary

```yaml
test-summary:
  name: 测试结果汇总
  runs-on: ubuntu-latest
  needs: [test-user-authentication, test-teacher-exam, test-student-exam, test-all-integration]
  if: always()  # 总是运行，即使前面有失败
  
  steps:
    - name: 下载所有测试报告
      uses: actions/download-artifact@v4
    
    - name: 生成汇总报告
      run: |
        # 生成详细的汇总报告
```

---

## 📈 测试执行流程

```
1. 触发Workflow（push/PR/手动）
   ↓
2. 并行运行3个测试任务
   ├─ test-user-authentication (6个用例)
   ├─ test-teacher-exam (8个用例)
   └─ test-student-exam (7个用例)
   ↓
3. 所有任务完成后运行
   └─ test-all-integration (21个用例)
   ↓
4. 生成汇总报告
   └─ test-summary
```

---

## 🎨 测试报告示例

### 测试摘要示例

```markdown
## 用户认证集成测试结果 🔐

- 📝 总测试数: 6
- ✅ 通过: 6
- ❌ 失败: 0
- ⚠️ 错误: 0
- ⏭️ 跳过: 0

### ✅ 状态: 全部通过
```

### 汇总报告示例

```markdown
# 🎯 集成测试汇总报告

**执行时间:** 2026-06-07 08:15:30 UTC
**触发方式:** push
**分支:** main
**提交:** `abc123def456`
**提交者:** CronuseDeBullard

## 测试任务执行情况

| 任务名称 | 状态 | 结果 |
|---------|------|------|
| 用户认证集成测试 | ✅ 成功 | 6个测试用例 |
| 教师端考试管理集成测试 | ✅ 成功 | 8个测试用例 |
| 学生端考试集成测试 | ✅ 成功 | 7个测试用例 |
| 所有集成测试 | ✅ 成功 | 21个测试用例 |
```

---

## 🔍 常见问题

### Q1: 测试失败怎么办？

**A:** 
1. 点击失败的任务查看详细日志
2. 下载测试报告查看失败原因
3. 在本地运行相同的测试：
   ```bash
   mvn test -Dtest=UserAuthenticationIntegrationTest
   ```
4. 修复问题后重新提交

### Q2: 如何只运行特定的测试？

**A:** 
1. 手动触发workflow
2. 或者修改workflow文件，注释掉不需要的job

### Q3: 测试运行太慢怎么办？

**A:** 
- 当前配置已经使用了Maven缓存
- 3个测试任务并行运行，节省时间
- 如果还是慢，可以考虑：
  - 减少测试用例
  - 使用更快的runner（付费）

### Q4: 如何查看历史测试结果？

**A:** 
1. 进入 **Actions** 标签
2. 左侧选择 "新集成测试"
3. 查看所有历史运行记录

### Q5: 测试报告保留多久？

**A:** 
- 默认保留30天
- 可以在workflow文件中修改 `retention-days`

---

## 📝 最佳实践

### 1. 提交前本地测试

```bash
# 在提交前先本地运行测试
mvn test -Dtest=*IntegrationTest

# 确保测试通过后再提交
git add .
git commit -m "添加新功能"
git push
```

### 2. 使用分支保护

在GitHub仓库设置中：
1. Settings → Branches
2. 添加分支保护规则
3. 勾选 "Require status checks to pass before merging"
4. 选择 "新集成测试" workflow

这样可以确保只有测试通过的代码才能合并到主分支。

### 3. 定期查看测试报告

- 每次push后查看测试结果
- 及时修复失败的测试
- 保持测试覆盖率

### 4. 优化测试性能

```yaml
# 使用Maven缓存
- name: 设置 JDK 17
  uses: actions/setup-java@v4
  with:
    cache: maven  # 缓存依赖，加快构建速度
```

---

## 🎯 下一步

### 1. 提交代码

```bash
cd "C:\Users\34445\Desktop\Software test"
git add .
git commit -m "添加集成测试和GitHub Actions配置"
git push origin main
```

### 2. 查看运行结果

访问：https://github.com/CronuseDeBullard/user-auth-test/actions

### 3. 查看测试摘要

点击最新的workflow运行，查看详细的测试报告

---

## 📚 相关文档

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

---

## ✅ 总结

您现在拥有：

1. ✅ 完整的集成测试（21个用例）
2. ✅ GitHub Actions自动化测试
3. ✅ 详细的测试报告
4. ✅ 测试结果汇总
5. ✅ 手动触发选项

**下一步：提交代码到GitHub，查看测试运行！** 🚀
