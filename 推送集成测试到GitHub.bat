@echo off
chcp 65001 >nul
echo ========================================
echo 推送集成测试到GitHub
echo ========================================
echo.

cd /d "C:\Users\34445\Desktop\Software test"

echo 当前目录: %CD%
echo.

echo ========================================
echo 检查Git状态
echo ========================================
echo.
git status
echo.

echo ========================================
echo 准备提交的文件
echo ========================================
echo.
echo 新创建的集成测试文件:
echo   - src/test/java/com/softwaretest/integration/UserAuthenticationIntegrationTest.java
echo   - src/test/java/com/softwaretest/integration/TeacherExamIntegrationTest.java
echo   - src/test/java/com/softwaretest/integration/StudentExamIntegrationTest.java
echo.
echo 新创建的文档:
echo   - 集成测试文档.md
echo   - 集成测试总结.md
echo   - GitHub Actions集成测试指南.md
echo   - 运行集成测试.bat
echo.
echo 新创建的GitHub Actions配置:
echo   - .github/workflows/new-integration-tests.yml
echo.

set /p confirm=是否继续提交? (Y/N): 

if /i not "%confirm%"=="Y" (
    echo.
    echo 取消提交
    pause
    exit /b 0
)

echo.
echo ========================================
echo 添加文件到Git
echo ========================================
echo.

REM 添加集成测试文件
git add src/test/java/com/softwaretest/integration/

REM 添加文档
git add "集成测试文档.md"
git add "集成测试总结.md"
git add "GitHub Actions集成测试指南.md"
git add "运行集成测试.bat"
git add "推送集成测试到GitHub.bat"

REM 添加GitHub Actions配置
git add .github/workflows/new-integration-tests.yml

echo 文件已添加
echo.

echo ========================================
echo 提交更改
echo ========================================
echo.

set /p commit_msg=请输入提交信息 (直接回车使用默认): 

if "%commit_msg%"=="" (
    set commit_msg=添加集成测试：用户认证、教师端、学生端（21个测试用例）
)

git commit -m "%commit_msg%"

if errorlevel 1 (
    echo.
    echo ❌ 提交失败！
    echo.
    pause
    exit /b 1
)

echo.
echo ✅ 提交成功！
echo.

echo ========================================
echo 推送到GitHub
echo ========================================
echo.

echo 正在推送到远程仓库...
echo.

git push origin main

if errorlevel 1 (
    echo.
    echo ❌ 推送失败！
    echo.
    echo 可能的原因:
    echo   1. 网络连接问题
    echo   2. 没有推送权限
    echo   3. 分支名称不是main（可能是master）
    echo.
    echo 尝试推送到master分支...
    git push origin master
    
    if errorlevel 1 (
        echo.
        echo ❌ 推送到master也失败！
        echo.
        echo 请手动执行: git push origin [分支名]
        pause
        exit /b 1
    )
)

echo.
echo ========================================
echo ✅ 推送成功！
echo ========================================
echo.

echo 🎉 集成测试已成功推送到GitHub！
echo.
echo 📊 查看测试运行:
echo    https://github.com/CronuseDeBullard/user-auth-test/actions
echo.
echo 💡 提示:
echo    1. 打开上面的链接查看GitHub Actions运行状态
echo    2. 点击最新的workflow查看测试结果
echo    3. 测试报告会在Artifacts中可供下载
echo.

pause
