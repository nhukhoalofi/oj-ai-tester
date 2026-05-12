@echo off
REM SETUP SCRIPT CHO SUBMISSION MODULE (Windows)

echo === OJ AI Tester - Submission Module Setup ===
echo.

REM Kiểm tra g++
echo 1. Checking g++ compiler...
where g++ >nul 2>nul
if %errorlevel% equ 0 (
    g++ --version | findstr /r "^"
    echo [OK] g++ found
) else (
    echo [ERROR] g++ not found!
    echo Please install MinGW-w64 from: https://www.mingw-w64.org/
    pause
    exit /b 1
)

echo.

REM Kiểm tra Java
echo 2. Checking Java...
where java >nul 2>nul
if %errorlevel% equ 0 (
    java -version 2>&1
    echo [OK] Java found
) else (
    echo [ERROR] Java not found!
    pause
    exit /b 1
)

echo.

REM Kiểm tra Maven
echo 3. Checking Maven...
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    mvn --version | findstr /r "^"
    echo [OK] Maven found
) else (
    echo [ERROR] Maven not found!
    pause
    exit /b 1
)

echo.

REM Tạo thư mục
echo 4. Creating directories...
if not exist "submissions" mkdir submissions
if not exist "submissions\testcases" mkdir submissions\testcases
echo [OK] Directory created: submissions/testcases

echo.

REM Build project
echo 5. Building project...
call mvn clean package -DskipTests

if %errorlevel% equ 0 (
    echo [OK] Build successful!
) else (
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo.
echo === Setup Complete ===
echo.
echo To run the application:
echo   mvn javafx:run
echo.
pause

