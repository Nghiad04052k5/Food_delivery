@echo off
echo =========================================
echo Compiling project and JUnit 5 tests...
echo =========================================

if not exist bin mkdir bin

javac -encoding UTF-8 -cp "lib/*;src" -d bin src/com/delivery/test/TestRunner.java
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed!
    exit /b %errorlevel%
)

echo.
echo =========================================
echo Running tests...
echo =========================================
java -cp "lib/*;bin" com.delivery.test.TestRunner
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Tests failed!
    exit /b %errorlevel%
)

echo.
echo [SUCCESS] All steps completed.
