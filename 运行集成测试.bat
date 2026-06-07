@echo off
chcp 65001 >nul
echo ========================================
echo 运行集成测试
echo ========================================
echo.

cd /d "C:\Users\34445\Desktop\Software test"

echo 当前目录: %CD%
echo.

echo ========================================
echo 选择测试类型
echo ========================================
echo 1. 运行所有集成测试
echo 2. 运行用户认证集成测试
echo 3. 运行教师端集成测试
echo 4. 运行学生端集成测试
echo 5. 运行所有测试（包括原有测试）
echo 6. 查看测试报告
echo ========================================
echo.

set /p choice=请输入选择 (1-6): 

if "%choice%"=="1" goto run_all_integration
if "%choice%"=="2" goto run_user_auth
if "%choice%"=="3" goto run_teacher
if "%choice%"=="4" goto run_student
if "%choice%"=="5" goto run_all
if "%choice%"=="6" goto view_report
goto invalid

:run_all_integration
echo.
echo ========================================
echo 运行所有集成测试...
echo ========================================
echo.
call mvn test -Dtest=*IntegrationTest
goto end

:run_user_auth
echo.
echo ========================================
echo 运行用户认证集成测试...
echo ========================================
echo.
call mvn test -Dtest=UserAuthenticationIntegrationTest
goto end

:run_teacher
echo.
echo ========================================
echo 运行教师端集成测试...
echo ========================================
echo.
call mvn test -Dtest=TeacherExamIntegrationTest
goto end

:run_student
echo.
echo ========================================
echo 运行学生端集成测试...
echo ========================================
echo.
call mvn test -Dtest=StudentExamIntegrationTest
goto end

:run_all
echo.
echo ========================================
echo 运行所有测试（包括原有测试）...
echo ========================================
echo.
call mvn clean test
goto end

:view_report
echo.
echo ========================================
echo 测试报告位置
echo ========================================
echo.
echo Surefire报告: target\surefire-reports\
echo.
if exist "target\surefire-reports\" (
    echo 打开报告目录...
    start "" "target\surefire-reports\"
) else (
    echo 报告目录不存在，请先运行测试
)
goto end

:invalid
echo.
echo 无效的选择！
echo.
pause
exit /b 1

:end
echo.
echo ========================================
echo 测试完成！
echo ========================================
echo.
echo 测试报告位置: target\surefire-reports\
echo.
pause
