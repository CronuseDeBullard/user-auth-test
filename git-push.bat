@echo off
chcp 65001 >nul
echo ========================================
echo Git 推送脚本
echo ========================================
echo.

REM 检查是否已初始化 Git
if not exist .git (
    echo [步骤 1] 初始化 Git 仓库...
    git init
    echo ✓ Git 仓库初始化完成
    echo.
) else (
    echo [步骤 1] Git 仓库已存在，跳过初始化
    echo.
)

REM 添加所有文件
echo [步骤 2] 添加所有文件到暂存区...
git add .
echo ✓ 文件添加完成
echo.

REM 显示状态
echo [步骤 3] 当前状态：
git status
echo.

REM 提交
echo [步骤 4] 提交更改...
set /p commit_msg="请输入提交信息 (直接回车使用默认信息): "
if "%commit_msg%"=="" (
    set commit_msg=Initial commit: 用户认证集成测试项目
)
git commit -m "%commit_msg%"
echo ✓ 提交完成
echo.

REM 检查是否已配置远程仓库
git remote -v >nul 2>&1
if errorlevel 1 (
    echo [步骤 5] 配置远程仓库...
    echo.
    echo 请输入您的 GitHub 仓库地址，格式如下：
    echo https://github.com/your-username/user-auth-test.git
    echo 或
    echo git@github.com:your-username/user-auth-test.git
    echo.
    set /p remote_url="远程仓库地址: "
    
    if not "%remote_url%"=="" (
        git remote add origin %remote_url%
        echo ✓ 远程仓库配置完成
    ) else (
        echo ✗ 未配置远程仓库，请手动执行：
        echo git remote add origin YOUR_REPO_URL
        pause
        exit /b 1
    )
) else (
    echo [步骤 5] 远程仓库已配置
    git remote -v
)
echo.

REM 设置主分支名称
echo [步骤 6] 设置主分支为 main...
git branch -M main
echo ✓ 分支设置完成
echo.

REM 推送
echo [步骤 7] 推送到 GitHub...
echo.
echo 正在推送，请稍候...
git push -u origin main

if errorlevel 1 (
    echo.
    echo ========================================
    echo ⚠️ 推送失败！
    echo ========================================
    echo.
    echo 可能的原因：
    echo 1. 远程仓库地址错误
    echo 2. 没有权限访问仓库
    echo 3. 网络连接问题
    echo 4. 需要先在 GitHub 创建仓库
    echo.
    echo 解决方法：
    echo 1. 检查远程仓库地址：git remote -v
    echo 2. 确保已在 GitHub 创建仓库
    echo 3. 检查 Git 凭据是否正确
    echo.
    pause
    exit /b 1
) else (
    echo.
    echo ========================================
    echo ✅ 推送成功！
    echo ========================================
    echo.
    echo 下一步：
    echo 1. 打开 GitHub 仓库页面
    echo 2. 点击 Actions 标签查看自动化测试
    echo 3. 等待测试完成并查看结果
    echo.
)

pause
